package com.ukonnra.wonderland.doorknob.core

import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(DoorKnobConfiguration::class, ReactiveOAuth2ResourceServerAutoConfiguration::class)
class TestApplicationConfiguration
