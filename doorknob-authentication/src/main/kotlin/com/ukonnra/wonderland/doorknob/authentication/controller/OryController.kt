package com.ukonnra.wonderland.doorknob.authentication.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ukonnra.wonderland.doorknob.authentication.AuthenticationService
import com.ukonnra.wonderland.doorknob.authentication.ConsentModel
import com.ukonnra.wonderland.doorknob.authentication.LoginModel
import com.ukonnra.wonderland.doorknob.authentication.PreConsentModel
import com.ukonnra.wonderland.doorknob.authentication.PreLoginModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import sh.ory.hydra.model.OAuth2Client

@RestController
class OryController @Autowired constructor(private val authentication: AuthenticationService) {
  @PostMapping("/clients")
  suspend fun createClients(): List<OAuth2Client> {
    return authentication.createClients()
  }

  @GetMapping("/login")
  suspend fun preLogin(
    @RequestParam("login_challenge") challenge: String,
    exchange: ServerWebExchange,
  ): ResponseEntity<PreLoginModel> = exchange.getRequiredAttribute<CsrfToken>(CsrfToken::class.java.name)
    .let {
      authentication.preLogin(challenge, it)
    }

  @GetMapping("/login/{specific-way}")
  fun specificWayLogin(
    @PathVariable("specific-way", required = false) specificWay: String,
    @RequestParam("login_challenge", required = false) challenge: String,
    exchange: ServerWebExchange,
  ): ResponseEntity<*> = exchange.getRequiredAttribute<CsrfToken>(CsrfToken::class.java.name)
    .let {
      authentication.specificWayLogin(specificWay, challenge, it)
    }

  @GetMapping("/login/{specific-way}/callback")
  suspend fun specificWayCallback(
    @PathVariable("specific-way") specificWay: String,
    @RequestParam params: Map<String, String>,
    exchange: ServerWebExchange,
  ): ResponseEntity<*> = exchange.getRequiredAttribute<CsrfToken>(CsrfToken::class.java.name)
    .let {
      authentication.specificWayCallback(specificWay, params, it)
    }

  @PostMapping("/login")
  suspend fun login(@RequestBody body: LoginModel): ResponseEntity<PreLoginModel> {
    return authentication.login(body)
  }

  @GetMapping("/consent")
  suspend fun preConsent(
    @RequestParam("consent_challenge") challenge: String,
    exchange: ServerWebExchange,
  ): ResponseEntity<PreConsentModel> = exchange.getRequiredAttribute<CsrfToken>(CsrfToken::class.java.name)
    .let {
      authentication.preConsent(challenge, it)
    }

  @PostMapping("/consent")
  suspend fun consent(@RequestBody body: ConsentModel): ResponseEntity<Unit> {
    return authentication.consent(body)
  }

  @RequestMapping("/user-info")
  suspend fun userInfo() = coroutineScope {
    val authToken =
      ReactiveSecurityContextHolder.getContext().awaitSingle().authentication.principal as OAuth2AuthenticatedPrincipal
    val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
    println("AuthToken - name: ${authToken.name}")

    val job1 = async(Dispatchers.IO) {
      println("AuthToken - attributes: ${writer.writeValueAsString(authToken.attributes)}")
    }

    val job2 = async(Dispatchers.IO) {
      println("AuthToken - authorities: ${writer.writeValueAsString(authToken.authorities)}")
    }

    awaitAll(job1, job2)
  }
}
