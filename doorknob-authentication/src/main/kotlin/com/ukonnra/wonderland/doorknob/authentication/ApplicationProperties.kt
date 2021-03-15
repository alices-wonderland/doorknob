package com.ukonnra.wonderland.doorknob.authentication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "doorknob.authentication")
@ConstructorBinding
data class ApplicationProperties(
  val adminUrl: String,
  val frontendUrl: String,
)
