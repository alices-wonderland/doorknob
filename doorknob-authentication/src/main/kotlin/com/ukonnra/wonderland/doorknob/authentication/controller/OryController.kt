package com.ukonnra.wonderland.doorknob.authentication.controller

import com.ukonnra.wonderland.doorknob.authentication.AuthenticationService
import com.ukonnra.wonderland.doorknob.authentication.ConsentModel
import com.ukonnra.wonderland.doorknob.authentication.LoginModel
import com.ukonnra.wonderland.doorknob.authentication.PreConsentModel
import com.ukonnra.wonderland.doorknob.authentication.PreLoginModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import sh.ory.hydra.model.OAuth2Client

@RestController
class OryController @Autowired constructor(private val authentication: AuthenticationService) {
  @PostMapping("/clients")
  fun createClients(): Mono<List<OAuth2Client>> {
    return authentication.createClients()
  }

  @GetMapping("/login")
  fun preLogin(
    @RequestParam("login_challenge") challenge: String,
    exchange: ServerWebExchange,
  ): Mono<ResponseEntity<PreLoginModel>> = exchange.getRequiredAttribute<Mono<CsrfToken>>(CsrfToken::class.java.name)
    .flatMap {
      authentication.preLogin(challenge, it)
    }

  @GetMapping("/login/{specific-way}")
  fun specificWayLogin(
    @PathVariable("specific-way", required = false) specificWay: String,
    @RequestParam("login_challenge", required = false) challenge: String,
    exchange: ServerWebExchange,
  ): Mono<ResponseEntity<*>> = exchange.getRequiredAttribute<Mono<CsrfToken>>(CsrfToken::class.java.name)
    .flatMap {
      authentication.specificWayLogin(specificWay, challenge, it)
    }

  @GetMapping("/login/{specific-way}/callback")
  fun specificWayCallback(
    @PathVariable("specific-way") specificWay: String,
    @RequestParam params: Map<String, String>,
    exchange: ServerWebExchange,
  ): Mono<ResponseEntity<*>> = exchange.getRequiredAttribute<Mono<CsrfToken>>(CsrfToken::class.java.name)
    .flatMap {
      authentication.specificWayCallback(specificWay, params, it)
    }

  @PostMapping("/login")
  fun login(@RequestBody body: LoginModel): Mono<ResponseEntity<PreLoginModel>> {
    return authentication.login(body)
  }

  @GetMapping("/consent")
  fun preConsent(
    @RequestParam("consent_challenge") challenge: String,
    exchange: ServerWebExchange,
  ): Mono<ResponseEntity<PreConsentModel>> = exchange.getRequiredAttribute<Mono<CsrfToken>>(CsrfToken::class.java.name)
    .flatMap {
      authentication.preConsent(challenge, it)
    }

  @PostMapping("/consent")
  fun consent(@RequestBody body: ConsentModel): Mono<ResponseEntity<Unit>> {
    return authentication.consent(body)
  }
}
