package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.doorknob.core.AppAuthUser
import com.ukonnra.wonderland.doorknob.core.Errors
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import com.ukonnra.wonderland.infrastructure.core.service.AbstractAggregateService
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import org.springframework.stereotype.Service

@Service
class UserService @Autowired constructor(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val introspector: ReactiveOpaqueTokenIntrospector,
  private val identifierActivator: IdentifierActivator,
) : AbstractAggregateService<UserAggregate.Id, UserAggregate, UserCommand, AppAuthUser> {
  @Suppress("ThrowsCount")
  suspend fun login(identType: Identifier.Type, identValue: String): UserAggregate.Id {
    val user = userRepository.getByIdentifier(identType, identValue)
      ?: throw WonderlandError.NotFound(UserAggregate.type, identValue)

    val data = user.userInfo as? UserAggregate.UserInfo.Created
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

  private suspend fun handle(command: UserCommand.StartCreate, cacheId: Long?): UserAggregate {
    if (userRepository.getByIdentifier(command.identType, command.identValue, cacheId = cacheId) != null) {
      throw WonderlandError.AlreadyExists(UserAggregate.type, command.identValue)
    }

    val result = UserAggregate.handle(command)
    userRepository.save(result)
    identifierActivator.activate(command.identType, command.identValue)
    return result
  }

  private suspend fun handle(command: UserCommand.RefreshCreate, cacheId: Long?): UserAggregate {
    val aggregate = userRepository.getById(command.targetId, cacheId = cacheId)
      ?: throw WonderlandError.NotFound(UserAggregate.type, command.targetId.value)

    val result = aggregate.handle(command)
    userRepository.save(result)

    val userInfo = result.userInfo as UserAggregate.UserInfo.Uncreated
    identifierActivator.activate(userInfo.identifier)
    return result
  }

  private suspend fun handle(command: UserCommand.FinishCreate, cacheId: Long?): UserAggregate {
    val aggregate = userRepository.getById(command.targetId, cacheId = cacheId)
      ?: throw WonderlandError.NotFound(UserAggregate.type, command.targetId.value)

    val result = aggregate.handle(command)
    userRepository.save(result)
    return result
  }

  private suspend fun handle(authUser: AppAuthUser, command: UserCommand.SuperCreate, cacheId: Long?): UserAggregate {
    if (userRepository.getByIdentifier(command.identifier.type, command.identifier.value, cacheId = cacheId) != null) {
      throw WonderlandError.AlreadyExists(UserAggregate.type, command.identifier.value)
    }

    val result = UserAggregate.handle(authUser, command)
    userRepository.save(result)
    return result
  }

  override suspend fun introspect(token: String, cacheId: Long?): AppAuthUser {
    val principal = introspector.introspect(token).awaitSingle()
    val user = userRepository.getById(UserAggregate.Id(principal.name), cacheId)
    return AppAuthUser(principal, user)
  }

  override suspend fun handle(authUser: AppAuthUser?, command: UserCommand, cacheId: Long?): UserAggregate {
    return when (command) {
      is UserCommand.SuperCreate -> authUser?.let { handle(it, command, cacheId) } ?: throw WonderlandError.NoAuth()

      is UserCommand.StartCreate -> handle(command, cacheId)
      is UserCommand.RefreshCreate -> handle(command, cacheId)
      is UserCommand.FinishCreate -> handle(command, cacheId)

      is UserCommand.StartActivateIdentifier -> TODO()
      is UserCommand.RefreshActivateIdentifier -> TODO()
      is UserCommand.FinishActivateIdentifier -> TODO()
      is UserCommand.DeactivateIdentifier -> TODO()

      is UserCommand.StartChangePassword -> TODO()
      is UserCommand.RefreshChangePassword -> TODO()
      is UserCommand.FinishChangePassword -> TODO()

      is UserCommand.SuperUpdate -> TODO()
      is UserCommand.Update -> TODO()
      is UserCommand.Delete -> TODO()
    }
  }
}
