package com.ukonnra.wonderland.doorknob.authentication.utils

import com.ukonnra.wonderland.doorknob.authentication.ApplicationProperties
import com.ukonnra.wonderland.doorknob.authentication.LoginModel
import com.ukonnra.wonderland.doorknob.authentication.SpecificWayLoginMeta
import com.ukonnra.wonderland.doorknob.core.Errors
import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

object SpecificWayUtils {
  private val LOGGER = LoggerFactory.getLogger(SpecificWayUtils::class.java)

  private const val GITHUB_AUTH_AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
  private const val GITHUB_AUTH_TOKEN_URL = "https://github.com/login/oauth/access_token"
  private const val GITHUB_AUTH_USER_URL = "https://api.github.com/user"

  fun parse(value: String): Mono<Identifier.SpecificWay> = when (val sw = Identifier.SpecificWay.fromUrlFormat(value)) {
    null -> Mono.error(Errors.SpecificWayNotActivated(value))
    else -> Mono.just(sw)
  }

  fun loginGithubAuth(props: ApplicationProperties, challenge: String, csrfToken: String): Mono<URI> =
    if (props.github == null) {
      Mono.error(Errors.SpecificWayNotActivated(Identifier.SpecificWay.GITHUB_AUTH))
    } else {
      LOGGER.info("Login Via Github: challenge: {}, csrf: {}", challenge, csrfToken)

      val uri = UriComponentsBuilder.fromHttpUrl(GITHUB_AUTH_AUTHORIZE_URL)
        .queryParam("client_id", props.github.clientId)
        .queryParam("redirect_uri", props.github.redirectUri)
        .queryParam("scope", "read:user")
        .queryParam("state", SpecificWayLoginMeta(challenge, csrfToken).encode())
        .build().toUri()

      Mono.just(uri)
    }

  fun callbackGithubAuth(props: ApplicationProperties, params: Map<String, String>, csrf: CsrfToken): Mono<LoginModel> {
    return when {
      props.github == null -> Mono.error(Errors.SpecificWayNotActivated(Identifier.SpecificWay.GITHUB_AUTH))
      params["code"] == null -> Mono.error(
        Errors.SpecificWayLoginMissingParam(Identifier.SpecificWay.GITHUB_AUTH, "code")
      )
      params["state"] == null -> Mono.error(
        Errors.SpecificWayLoginMissingParam(Identifier.SpecificWay.GITHUB_AUTH, "state")
      )
      else -> {
        val code = params["code"]!!
        val state = params["state"]!!

        val meta = SpecificWayLoginMeta.decode(state)

        LOGGER.info("Callback from Github: code: {}, state: {}, csrf: {}, meta: {}", code, state, csrf.token, meta)

        val uri = UriComponentsBuilder.fromUriString(GITHUB_AUTH_TOKEN_URL)
          .queryParam("client_id", props.github.clientId)
          .queryParam("client_secret", props.github.clientSecret)
          .queryParam("code", code)
          .queryParam("state", state)
          .build()

        WebClient.create(GITHUB_AUTH_TOKEN_URL).post().uri(uri.toUri())
          .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
          .exchangeToMono { it.bodyToMono<Map<String, String>>() }
          .flatMap {
            LOGGER.info("ACCESS_TOKEN: {}", it)
            val tokenType = it["token_type"]
              ?: return@flatMap Mono.error(
                Errors.SpecificWayLoginMissingParam(Identifier.SpecificWay.GITHUB_AUTH, "token_type")
              )
            val accessToken = it["access_token"]
              ?: return@flatMap Mono.error(
                Errors.SpecificWayLoginMissingParam(Identifier.SpecificWay.GITHUB_AUTH, "access_token")
              )

            WebClient.create(GITHUB_AUTH_USER_URL).get()
              .header(HttpHeaders.AUTHORIZATION, "$tokenType $accessToken")
              .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
              .exchangeToMono { resp -> resp.bodyToMono<Map<String, Any>>() }
          }.flatMap {
            LOGGER.info("USER: {}", it)
            val id =
              it["id"] ?: return@flatMap Mono.error(
                Errors.SpecificWayLoginMissingParam(
                  Identifier.SpecificWay.GITHUB_AUTH,
                  "id"
                )
              )

            Mono.just(LoginModel(csrf.token, meta.challenge, Identifier.Type.GITHUB, id.toString(), true))
          }
      }
    }
  }
}