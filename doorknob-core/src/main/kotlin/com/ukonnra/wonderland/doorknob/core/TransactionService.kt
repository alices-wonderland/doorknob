package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.UserCommand
import com.ukonnra.wonderland.doorknob.core.domain.user.UserRepository
import com.ukonnra.wonderland.doorknob.core.domain.user.UserService
import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.Command
import com.ukonnra.wonderland.infrastructure.core.locker.AbstractLocker
import com.ukonnra.wonderland.infrastructure.core.service.AbstractTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Service

@Service
class TransactionService @Autowired constructor(
  private val locker: AbstractLocker,
  private val userRepository: UserRepository,
  private val userService: UserService,
) : AbstractTransactionService {
  override suspend fun handleCommands(authToken: OAuth2AuthenticatedPrincipal, commands: List<Command<*, *>>):
    List<Aggregate<*>> = locker.withLock("DoorKnob") { cacheId ->
    val result = mutableListOf<Aggregate<*>>()
    val operator = AppAuthUser(authToken, userRepository.getById(UserAggregate.Id(authToken.name), cacheId))
    for (command in commands) {
      val aggregate = when (command) {
        is UserCommand -> userService.handle(operator, command, cacheId)
        else -> null
      }
      aggregate?.let {
        userRepository.saveAll(listOf(it), cacheId)
        result.add(it)
      }
    }

    userRepository.clearCache(cacheId)

    result
  }
}
