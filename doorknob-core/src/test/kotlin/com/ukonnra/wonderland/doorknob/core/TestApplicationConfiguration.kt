package com.ukonnra.wonderland.doorknob.core

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector

@Configuration
@Import(DoorKnobConfiguration::class, ReactiveOAuth2ResourceServerAutoConfiguration::class)
class TestApplicationConfiguration {
  @Value("\${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
  private lateinit var clientId: String
  @Value("\${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
  private lateinit var clientSecret: String
  @Value("\${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}")
  private lateinit var introspectionUri: String

  @Bean
  fun opaqueTokenIntrospector(): ReactiveOpaqueTokenIntrospector {
    return NimbusReactiveOpaqueTokenIntrospector(
      introspectionUri,
      clientId, clientSecret
    )
  }
}
