package com.ukonnra.wonderland.doorknob.core

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DoorKnobConfiguration {
  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
