package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.doorknob.core.AppAuthUser
import com.ukonnra.wonderland.doorknob.core.Errors
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import com.ukonnra.wonderland.infrastructure.core.service.AbstractAggregateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Service

@Service
class UserService @Autowired constructor(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder
) : AbstractAggregateService<UserAggregate.Id, UserAggregate, UserCommand, AppAuthUser> {
  private suspend fun validateIdentifier(identType: Identifier.Type, identValue: String) {
    TODO()
  }

  @Throws(AbstractError::class)
  suspend fun handle(operator: AppAuthUser?, command: UserCommand.SuperCreate, cacheId: Long? = null): UserAggregate {
    return when (userRepository.getByIdentifier(command.identifier.type, command.identifier.value, cacheId = cacheId)) {
      null -> operator?.let { UserAggregate.handle(it, command) } ?: throw WonderlandError.NoAuth()
      else -> throw WonderlandError.AlreadyExists(UserAggregate.type, command.identifier.value)
    }
  }

  @Throws(AbstractError::class)
  suspend fun handle(command: UserCommand.StartCreate, cacheId: Long? = null): UserAggregate {
    val user = when (userRepository.getByIdentifier(command.identType, command.identValue, cacheId = cacheId)) {
      null -> UserAggregate.handle(command)
      else -> throw WonderlandError.AlreadyExists(UserAggregate.type, command.identValue)
    }
    validateIdentifier(command.identType, command.identValue)
    return user
  }

  @Throws(AbstractError::class)
  suspend fun handle(command: UserCommand.RefreshCreate, cacheId: Long? = null): UserAggregate {
    val aggregate = userRepository.getById(command.targetId, cacheId)?.handleRefreshCreate()
      ?: throw WonderlandError.NotFound(UserAggregate.type, command.targetId.value)
    val data = aggregate.data as UserAggregate.Data.Uncreated

    validateIdentifier(data.identifier.type, data.identifier.value)
    return aggregate
  }

  @Throws(AbstractError::class)
  suspend fun handle(command: UserCommand.FinishCreate, cacheId: Long? = null): UserAggregate {
    return userRepository.getById(command.targetId, cacheId)?.handle(command)
      ?: throw WonderlandError.NotFound(UserAggregate.type, command.targetId.value)
  }

  @Throws(AbstractError::class)
  suspend fun handle(operator: AppAuthUser, command: UserCommand.Update, cacheId: Long? = null): UserAggregate {
    return userRepository.getById(command.targetId, cacheId)?.handle(operator, command)
      ?: throw WonderlandError.NotFound(UserAggregate.type, command.targetId.value)
  }

  @Throws(AbstractError::class)
  suspend fun handle(authToken: OAuth2AuthenticatedPrincipal?, command: UserCommand): UserAggregate {
    val operator = authToken?.let { a -> AppAuthUser(a, userRepository.getById(UserAggregate.Id(a.name))) }

    val result = handle(operator, command)

    userRepository.saveAll(listOf(result))

    return result
  }

  @Suppress("ThrowsCount")
  suspend fun login(identType: Identifier.Type, identValue: String): UserAggregate.Id {
    val user = userRepository.getByIdentifier(identType, identValue)
      ?: throw WonderlandError.NotFound(UserAggregate.type, identValue)

    val data = user.data as? UserAggregate.Data.Created
      ?: throw WonderlandError.NotFound(UserAggregate.type, identValue)

    val identifier = data.identifiers[identType] ?: throw Errors.IdentifierNotActivated(user.id, identValue)

    if (identifier.value != identValue || !passwordEncoder.matches(identifier.value, data.password)) {
      throw WonderlandError.NoAuth(user.id)
    }

    return user.id
  }

  suspend fun consent(userId: UserAggregate.Id) {
    userRepository.getById(userId)
      ?: throw WonderlandError.NotFound(UserAggregate.type, userId.value)
  }

  override suspend fun handle(operator: AppAuthUser?, command: UserCommand, cacheId: Long?): UserAggregate {
    TODO("Not yet implemented")
  }
}
