package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.UserRepository
import com.ukonnra.wonderland.doorknob.core.domain.user.UserService
import com.ukonnra.wonderland.doorknob.core.domain.user.impl.InMemoryUserRepository
import com.ukonnra.wonderland.infrastructure.core.locker.AbstractLocker
import com.ukonnra.wonderland.infrastructure.core.locker.RedissonLocker
import org.redisson.api.RedissonClient
import org.redisson.spring.starter.RedissonAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@ComponentScan(basePackageClasses = [TransactionService::class, UserService::class])
@Import(RedissonAutoConfiguration::class, ReactiveOAuth2ResourceServerAutoConfiguration::class)
class DoorKnobConfiguration @Autowired constructor(private val redisson: RedissonClient) {
  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun locker(): AbstractLocker = RedissonLocker(redisson)

  @Bean
  open fun userRepository(): UserRepository = InMemoryUserRepository()
}
