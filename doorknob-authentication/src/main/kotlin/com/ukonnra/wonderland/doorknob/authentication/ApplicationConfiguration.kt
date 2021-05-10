package com.ukonnra.wonderland.doorknob.authentication

import com.ukonnra.wonderland.doorknob.core.DoorKnobConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.ReactiveSessionRepository
import org.springframework.session.Session
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import sh.ory.hydra.ApiException as HydraException

@Configuration
@EnableSpringWebSession
@EnableWebFluxSecurity
@Import(DoorKnobConfiguration::class, RedissonNativeHints::class)
@EnableConfigurationProperties(ApplicationProperties::class)
@ComponentScan(basePackageClasses = [AuthenticationService::class])
open class ApplicationConfiguration @Autowired constructor(private val introspector: ReactiveOpaqueTokenIntrospector) {
  @Bean
  open fun reactiveSessionRepository(): ReactiveSessionRepository<out Session> {
    return ReactiveMapSessionRepository(ConcurrentHashMap())
  }

  @Bean
  open fun globalErrorHandler(): ErrorWebExceptionHandler {
    return ErrorWebExceptionHandler { exchange, ex ->
      val resp = exchange.response
      resp.statusCode = HttpStatus.BAD_REQUEST
      val body: ByteArray? = when (ex) {
        is HydraException -> {
          resp.rawStatusCode = ex.code
          ex.responseBody.toByteArray(StandardCharsets.UTF_8)
        }
        else -> null
      }
      when {
        body != null -> {
          val buffer = resp.bufferFactory().wrap(body)
          resp.writeWith(Mono.just(buffer))
        }
        else -> resp.setComplete()
      }
    }
  }

  @Bean
  open fun securityWebFilterChain(
    http: ServerHttpSecurity
  ): SecurityWebFilterChain {
    return http
      .csrf {
        val repo = CookieServerCsrfTokenRepository()
        it.csrfTokenRepository(repo)
        it.requireCsrfProtectionMatcher { exc ->
          val req = exc.request
          if (req.method == HttpMethod.GET ||
            (req.method == HttpMethod.POST && req.path.value() == "/clients")
          ) {
            ServerWebExchangeMatcher.MatchResult.notMatch()
          } else {
            ServerWebExchangeMatcher.MatchResult.match()
          }
        }
      }
      .authorizeExchange {
        it.pathMatchers("/login/**", "/favicon.ico").permitAll()
          .pathMatchers(HttpMethod.POST, "/clients").permitAll()
          .pathMatchers(HttpMethod.GET, "/health").permitAll()
          .anyExchange().authenticated()
      }
      .oauth2ResourceServer {
        it.opaqueToken { token ->
          token.introspector { tokenStr ->
            println("Token from introspector: $tokenStr")
            introspector.introspect(tokenStr)
              .doOnSuccess { res ->
                println("Success from introspector: $res")
              }.doOnError { err ->
                println("Error from introspector: $err")
              }
          }
        }
      }
      .build()
  }
}
