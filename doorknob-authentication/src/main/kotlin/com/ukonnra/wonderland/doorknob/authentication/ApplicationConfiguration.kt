package com.ukonnra.wonderland.doorknob.authentication

import com.fasterxml.jackson.datatype.threetenbp.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.threetenbp.ser.OffsetDateTimeSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.ReactiveSessionRepository
import org.springframework.session.Session
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.threeten.bp.format.DateTimeFormatter
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import sh.ory.hydra.ApiException as HydraException

@Configuration
@EnableSpringWebSession
@EnableWebFluxSecurity
class ApplicationConfiguration {
  @Bean
  fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
    return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
      builder
        .serializers(LocalDateTimeSerializer(DateTimeFormatter.ISO_INSTANT))
        .serializers(OffsetDateTimeSerializer.INSTANCE)
    }
  }

  @Bean
  fun reactiveSessionRepository(): ReactiveSessionRepository<out Session> {
    return ReactiveMapSessionRepository(ConcurrentHashMap())
  }

  @Bean
  fun globalErrorHandler(): ErrorWebExceptionHandler {
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
  fun securityWebFilterChain(
    http: ServerHttpSecurity
  ): SecurityWebFilterChain {
    return http.authorizeExchange {
      it.pathMatchers("/login").permitAll()
        .anyExchange().authenticated()
    }.build()
  }
}
