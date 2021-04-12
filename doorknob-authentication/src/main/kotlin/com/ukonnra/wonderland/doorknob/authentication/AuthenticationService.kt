package com.ukonnra.wonderland.doorknob.authentication

import com.ukonnra.wonderland.doorknob.authentication.utils.SpecificWayUtils
import com.ukonnra.wonderland.doorknob.core.domain.client.Client
import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserService
import com.ukonnra.wonderland.infrastructure.core.error.ExternalError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
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

@Service
class AuthenticationService @Autowired constructor(
  private val props: ApplicationProperties,
  private val userService: UserService,
) {
  private final val admin: AdminApi
  private final val frontUrl: URI

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AuthenticationService::class.java)
    private const val REMEMBER_FOR = 3600L
  }

  init {
    val client = ApiClient().setBasePath(props.adminUrl)
    admin = AdminApi(client)
    frontUrl = URI.create(props.frontendUrl)
  }

  fun createClients(): Mono<List<OAuth2Client>> {
    LOGGER.info("Create Clients")
    return toMono<Void> { admin.deleteOAuth2ClientAsync("absolem", it) }
      .onErrorResume { Mono.empty() }
      .then(toMono<Void> { admin.deleteOAuth2ClientAsync("absolem-ui", it) })
      .onErrorResume { Mono.empty() }
      .then(
        toMono<OAuth2Client> {
          val client = OAuth2Client()
            .clientId("absolem")
            .clientName("Absolem Backend Client")
            .clientSecret("test-secret")
            .grantTypes(listOf("client_credentials"))
            .responseTypes(listOf("code"))
            .scope("openid offline")
          admin.createOAuth2ClientAsync(client, it)
        }
      )
      .then(
        toMono<OAuth2Client> {
          val client = OAuth2Client()
            .clientId("absolem-ui")
            .clientName("Absolem Frontend Client")
            .redirectUris(listOf("http://127.0.0.1:3000/callback"))
            .grantTypes(listOf("authorization_code", "refresh_token"))
            .responseTypes(listOf("code", "token", "id_token"))
            .scope("openid offline profile:read email")
            .tokenEndpointAuthMethod("none")
            .metadata(setOf(Client.MetaItem.SKIP_CONSENT))
          admin.createOAuth2ClientAsync(client, it)
        }
      )
      .then(
        Flux.fromIterable(
          listOf<Mono<OAuth2Client>>(
            toMono { admin.getOAuth2ClientAsync("absolem", it) },
            toMono { admin.getOAuth2ClientAsync("absolem-ui", it) }
          ),
        )
          .flatMap { it }
          .collectList()
      )
  }

  fun preLogin(challenge: String, csrfToken: CsrfToken): Mono<ResponseEntity<PreLoginModel>> =
    toMono<LoginRequest> { admin.getLoginRequestAsync(challenge, it) }
      .flatMap { req ->
        if (req.skip) {
          toMono<CompletedRequest> {
            admin.acceptLoginRequestAsync(challenge, AcceptLoginRequest().subject(req.subject), it)
          }
            .map {
              ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(URI.create(it.redirectTo))
                .build()
            }
        } else {
          Mono.just(
            ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_JSON)
              .body(PreLoginModel(csrfToken.token, challenge, req.oidcContext?.loginHint?.toError()))
          )
        }
      }

  fun login(body: LoginModel): Mono<ResponseEntity<PreLoginModel>> {
    return userService.login(body.identType, body.identValue)
      .flatMap { id ->
        toMono<LoginRequest> { admin.getLoginRequestAsync(body.challenge, it) }
          .flatMap {
            LOGGER.info("Login Request: {}", it)
            toMono<CompletedRequest> { cb ->
              (
                admin.acceptLoginRequestAsync(
                  body.challenge,
                  AcceptLoginRequest().subject(id.toString()).remember(body.remember)
                    .rememberFor(REMEMBER_FOR).acr("0"),
                  cb
                )
                )
            }
          }
      }
      .map {
        LOGGER.info("Finish Login: {}", it)
        LOGGER.info("  Redirect to: {}", it.redirectTo)
        ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
          .location(URI.create(it.redirectTo))
          .build()
      }
  }

  fun preConsent(challenge: String, csrf: CsrfToken): Mono<ResponseEntity<PreConsentModel>> {
    return toMono<ConsentRequest> { admin.getConsentRequestAsync(challenge, it) }
      .flatMap { req ->
        val meta = req.client?.metadata as? Set<*>
        if (req.skip == true || meta?.contains(Client.MetaItem.SKIP_CONSENT) == true) {
          toMono<CompletedRequest> {
            admin.acceptConsentRequestAsync(
              challenge,
              AcceptConsentRequest()
                .grantScope(req.requestedScope)
                .grantAccessTokenAudience(req.requestedAccessTokenAudience),
              it
            )
          }.map {
            ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
              .location(URI.create(it.redirectTo))
              .build()
          }
        } else {
          Mono.just(
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
          )
        }
      }
  }

  fun consent(body: ConsentModel): Mono<ResponseEntity<Unit>> {
    if (!body.accept) {
      return toMono<CompletedRequest> {
        admin.rejectConsentRequestAsync(
          body.challenge,
          RejectRequest().error("access_denied").errorDescription("The resource owner denied the request"),
          it
        )
      }
        .map {
          ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(it.redirectTo)).build()
        }
    }

    return userService.consent(body.user).then(
      toMono<CompletedRequest> {
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
    )
      .map {
        ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(it.redirectTo)).build()
      }
  }

  fun specificWayLogin(specificWay: String, challenge: String, csrf: CsrfToken): Mono<ResponseEntity<*>> =
    SpecificWayUtils.parse(specificWay)
      .flatMap {
        when (it!!) {
          Identifier.SpecificWay.EMAIL_SEND -> TODO()
          Identifier.SpecificWay.PHONE_CALL -> TODO()
          Identifier.SpecificWay.PHONE_SEND_MESSAGE -> TODO()
          Identifier.SpecificWay.GITHUB_AUTH -> SpecificWayUtils.loginGithubAuth(props, challenge, csrf.token)
        }
      }.map {
        ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
          .location(it)
          .build<Any>()
      }

  fun specificWayCallback(specificWay: String, params: Map<String, String>, csrf: CsrfToken): Mono<ResponseEntity<*>> =
    SpecificWayUtils.parse(specificWay)
      .flatMap {
        when (it!!) {
          Identifier.SpecificWay.EMAIL_SEND -> TODO()
          Identifier.SpecificWay.PHONE_CALL -> TODO()
          Identifier.SpecificWay.PHONE_SEND_MESSAGE -> TODO()
          Identifier.SpecificWay.GITHUB_AUTH -> SpecificWayUtils.callbackGithubAuth(props, params, csrf)
        }
      }
      .flatMap {
        this.login(it)
      }
}

@Suppress("EmptyFunctionBlock")
private inline fun <reified T> toMono(crossinline fn: (ApiCallback<T>) -> Unit): Mono<T> {
  return Mono.create { sink: MonoSink<T> ->
    try {
      fn(
        object : ApiCallback<T> {
          override fun onFailure(e: ApiException, i: Int, map: Map<String, List<String>>) {
            println("Error onFailure: ${e.message}")
            sink.error(e)
          }

          override fun onSuccess(t: T, i: Int, map: Map<String, List<String>>) {
            sink.success(t)
          }

          override fun onUploadProgress(l: Long, l1: Long, b: Boolean) {}
          override fun onDownloadProgress(l: Long, l1: Long, b: Boolean) {}
        })
    } catch (e: ApiException) {
      sink.error(e)
    }
  }
}

private fun String.toError() = ExternalError("Hydra Error", message = this)
