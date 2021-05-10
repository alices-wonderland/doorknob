package com.ukonnra.wonderland.doorknob.authentication

import com.ukonnra.wonderland.doorknob.authentication.utils.SpecificWayUtils
import com.ukonnra.wonderland.doorknob.core.domain.client.ClientAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.UserService
import com.ukonnra.wonderland.infrastructure.core.error.ExternalError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.stereotype.Service
import sh.ory.hydra.ApiCallback
import sh.ory.hydra.ApiClient
import sh.ory.hydra.ApiException
import sh.ory.hydra.api.AdminApi
import sh.ory.hydra.model.AcceptConsentRequest
import sh.ory.hydra.model.AcceptLoginRequest
import sh.ory.hydra.model.CompletedRequest
import sh.ory.hydra.model.ConsentRequest
import sh.ory.hydra.model.ConsentRequestSession
import sh.ory.hydra.model.LoginRequest
import sh.ory.hydra.model.OAuth2Client
import sh.ory.hydra.model.RejectRequest
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Service
class AuthenticationService @Autowired constructor(
  private val props: ApplicationProperties,
  private val userService: UserService,
) {
  private val admin: AdminApi
  private val frontUrl: URI

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AuthenticationService::class.java)
    private const val REMEMBER_FOR = 3600L
  }

  init {
    val client = ApiClient().setBasePath(props.adminUrl)
    admin = AdminApi(client)
    frontUrl = URI.create(props.frontendUrl)
  }

  suspend fun createClients(): List<OAuth2Client> {
    LOGGER.info("Create Clients")

    try {
      await<Void> { admin.deleteOAuth2ClientAsync("absolem", it) }
      await<Void> { admin.deleteOAuth2ClientAsync("absolem-ui", it) }
    } catch (ignored: ApiException) {
    }

    await<OAuth2Client> {
      val client = OAuth2Client()
        .clientId("absolem")
        .clientName("Absolem Backend Client")
        .clientSecret("test-secret")
        .grantTypes(listOf("client_credentials"))
        .responseTypes(listOf("code"))
        .scope("openid offline")
      admin.createOAuth2ClientAsync(client, it)
    }
    await<OAuth2Client> {
      val client = OAuth2Client()
        .clientId("absolem-ui")
        .clientName("Absolem Frontend Client")
        .redirectUris(listOf("http://127.0.0.1:3000/callback"))
        .grantTypes(listOf("authorization_code", "refresh_token"))
        .responseTypes(listOf("code", "token", "id_token"))
        .scope("openid offline profile:read email")
        .tokenEndpointAuthMethod("none")
        .metadata(setOf(ClientAggregate.MetaItem.SKIP_CONSENT))
      admin.createOAuth2ClientAsync(client, it)
    }

    return listOf(
      await { admin.getOAuth2ClientAsync("absolem", it) },
      await { admin.getOAuth2ClientAsync("absolem-ui", it) }
    )
  }

  suspend fun preLogin(challenge: String, csrfToken: CsrfToken): ResponseEntity<PreLoginModel> {
    val req = await<LoginRequest> { admin.getLoginRequestAsync(challenge, it) }
    return if (req.skip) {
      val acc = await<CompletedRequest> {
        admin.acceptLoginRequestAsync(challenge, AcceptLoginRequest().subject(req.subject), it)
      }
      ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
        .location(URI.create(acc.redirectTo))
        .build()
    } else {
      ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(PreLoginModel(csrfToken.token, challenge, req.oidcContext?.loginHint?.toError()))
    }
  }

  suspend fun login(body: LoginModel): ResponseEntity<PreLoginModel> {
    val id = userService.login(body.identType, body.identValue)
    await<LoginRequest> { admin.getLoginRequestAsync(body.challenge, it) }
    val res = await<CompletedRequest> { cb ->
      (
        admin.acceptLoginRequestAsync(
          body.challenge,
          AcceptLoginRequest().subject(id.value).remember(body.remember)
            .rememberFor(REMEMBER_FOR).acr("0"),
          cb
        )
        )
    }
    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
      .location(URI.create(res.redirectTo))
      .build()
  }

  suspend fun preConsent(challenge: String, csrf: CsrfToken): ResponseEntity<PreConsentModel> {
    val req = await<ConsentRequest> { admin.getConsentRequestAsync(challenge, it) }
    val meta = req.client?.metadata as? Set<*>
    return if (req.skip == true || meta?.contains(ClientAggregate.MetaItem.SKIP_CONSENT) == true) {
      val res = await<CompletedRequest> {
        admin.acceptConsentRequestAsync(
          challenge,
          AcceptConsentRequest()
            .grantScope(req.requestedScope)
            .grantAccessTokenAudience(req.requestedAccessTokenAudience),
          it
        )
      }
      ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
        .location(URI.create(res.redirectTo))
        .build()
    } else {
      ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          PreConsentModel(
            csrf.token,
            challenge,
            req.requestedScope!!,
            req.subject!!,
            req.client?.clientId!!,
          )
        )
    }
  }

  suspend fun consent(body: ConsentModel): ResponseEntity<Unit> {
    return if (!body.accept) {
      await<CompletedRequest> {
        admin.rejectConsentRequestAsync(
          body.challenge,
          RejectRequest().error("access_denied").errorDescription("The resource owner denied the request"),
          it
        )
      }
        .let {
          ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(it.redirectTo)).build<Unit>()
        }
    } else {
      userService.consent(UserAggregate.Id(body.user))
      await<CompletedRequest> {
        admin.acceptConsentRequestAsync(
          body.challenge,
          AcceptConsentRequest()
            .grantScope(body.grantScopes)
            .session(ConsentRequestSession())
            .grantAccessTokenAudience(body.requestedAccessTokenAudience)
            .remember(body.remember)
            .rememberFor(REMEMBER_FOR),
          it
        )
      }
        .let {
          ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(it.redirectTo)).build()
        }
    }
  }

  fun specificWayLogin(specificWay: String, challenge: String, csrf: CsrfToken): ResponseEntity<*> =
    SpecificWayUtils.parse(specificWay)
      .let {
        when (it) {
          Identifier.SpecificWay.EMAIL_SEND -> TODO()
          Identifier.SpecificWay.PHONE_CALL -> TODO()
          Identifier.SpecificWay.PHONE_SEND_MESSAGE -> TODO()
          Identifier.SpecificWay.GITHUB_AUTH -> SpecificWayUtils.loginGithubAuth(props, challenge, csrf.token)
        }
      }.let {
        ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
          .location(it)
          .build<Any>()
      }

  suspend fun specificWayCallback(
    specificWay: String,
    params: Map<String, String>,
    csrf: CsrfToken
  ): ResponseEntity<*> =
    SpecificWayUtils.parse(specificWay)
      .let {
        when (it) {
          Identifier.SpecificWay.EMAIL_SEND -> TODO()
          Identifier.SpecificWay.PHONE_CALL -> TODO()
          Identifier.SpecificWay.PHONE_SEND_MESSAGE -> TODO()
          Identifier.SpecificWay.GITHUB_AUTH -> SpecificWayUtils.callbackGithubAuth(props, params, csrf)
        }
      }
      .let {
        this.login(it)
      }
}

@Suppress("EmptyFunctionBlock")
private suspend inline fun <reified T> await(crossinline fn: (ApiCallback<T>) -> Unit): T {
  return suspendCoroutine<T> {
    try {
      fn(
        object : ApiCallback<T> {
          override fun onFailure(ex: ApiException, i: Int, map: Map<String, List<String>>) {
            println("Error onFailure: ${ex.message}")
            it.resumeWithException(ex)
          }

          override fun onSuccess(t: T, i: Int, map: Map<String, List<String>>) {
            it.resume(t)
          }

          override fun onUploadProgress(l: Long, l1: Long, b: Boolean) {}
          override fun onDownloadProgress(l: Long, l1: Long, b: Boolean) {}
        })
    } catch (ex: ApiException) {
      it.resumeWithException(ex)
    }
  }
}

private fun String.toError() = ExternalError("Hydra Error", message = this)
