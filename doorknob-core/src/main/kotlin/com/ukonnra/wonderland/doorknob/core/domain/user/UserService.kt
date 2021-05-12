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
@Suppress("ThrowsCount", "ComplexMethod", "TooManyFunctions")
class UserService @Autowired constructor(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val introspector: ReactiveOpaqueTokenIntrospector,
  private val identifierActivator: IdentifierActivator,
) : AbstractAggregateService<UserAggregate.Id, UserAggregate, UserCommand, AppAuthUser> {
  suspend fun login(identType: Identifier.Type, identValue: String): UserAggregate.Id {
    val user = userRepository.getByIdentifier(identType, identValue)
      ?: throw WonderlandError.NotFound(UserAggregate.type, identValue)

    val data = user.userInfo as? UserAggregate.UserInfo.Created
      ?: throw WonderlandError.NotFound(UserAggregate.type, identValue)

    val identifier = data.identifiers[identType] ?: throw Errors.IdentifierNotExist(user.id, identValue)

    if (identifier.value != identValue || !passwordEncoder.matches(identifier.value, data.password.value)) {
      throw WonderlandError.NoAuth(user.id)
    }

    return user.id
  }

  suspend fun consent(userId: UserAggregate.Id) {
    userRepository.getById(userId)
      ?: throw WonderlandError.NotFound(UserAggregate.type, userId.value)
  }

  private suspend fun doHandle(
    identType: Identifier.Type,
    identValue: String,
    cacheId: Long?,
    fn: suspend () -> UserAggregate
  ): UserAggregate {
    if (userRepository.getByIdentifier(identType, identValue, cacheId = cacheId) != null) {
      throw WonderlandError.AlreadyExists(UserAggregate.type, identValue)
    }

    return fn().apply {
      userRepository.save(this)
    }
  }

  private suspend fun doHandle(
    id: UserAggregate.Id,
    cacheId: Long?,
    fn: suspend (UserAggregate) -> UserAggregate
  ): UserAggregate {
    val aggregate = userRepository.getById(id, cacheId = cacheId)
      ?: throw WonderlandError.NotFound(UserAggregate.type, id.value)

    return fn(aggregate).apply {
      userRepository.save(this)
    }
  }

  private suspend fun handle(authUser: AppAuthUser, command: UserCommand.SuperCreate, cacheId: Long?) =
    doHandle(command.identifier.type, command.identifier.value, cacheId) {
      UserAggregate.handle(authUser, command)
    }

  private suspend fun handle(command: UserCommand.StartCreate, cacheId: Long?) =
    doHandle(command.identType, command.identValue, cacheId) {
      val (result, identifier) = UserAggregate.handle(command)
      identifierActivator.activate(identifier)
      result
    }

  private suspend fun handle(command: UserCommand.RefreshCreate, cacheId: Long?) = doHandle(command.targetId, cacheId) {
    val (result, ident) = it.refreshCreate()
    identifierActivator.activate(ident)
    result
  }

  private suspend fun handle(command: UserCommand.FinishCreate, cacheId: Long?) = doHandle(command.targetId, cacheId) {
    it.finishCreate(command.code, command.nickname, command.password)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.UpdateBasicInfo,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.updateBasicInfo(authUser, command.nickname)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.StartChangePassword,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    val (result, ident) = it.startChangePassword(authUser, command.identType)
    identifierActivator.activate(ident)
    result
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.RefreshChangePassword,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    val (result, ident) = it.refreshChangePassword(authUser)
    identifierActivator.activate(ident)
    result
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.FinishChangePassword,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.finishChangePassword(authUser, command.code, command.password)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.SuperUpdate,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.superUpdate(authUser, command.nickname, command.password, command.identifiers, command.role)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.StartActivateIdentifier,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.startActivateIdentifier(authUser, command.identType, command.identValue)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.RefreshActivateIdentifier,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.refreshActivateIdentifier(authUser, command.identType)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.FinishActivateIdentifier,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.finishActivateIdentifier(authUser, command.identType, command.code)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.DeactivateIdentifier,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.deactivateIdentifier(authUser, command.identType)
  }

  private suspend fun handle(
    authUser: AppAuthUser,
    command: UserCommand.Delete,
    cacheId: Long?
  ) = doHandle(command.targetId, cacheId) {
    it.delete(authUser)
  }

  override suspend fun handle(authUser: AppAuthUser?, command: UserCommand, cacheId: Long?): UserAggregate {
    suspend fun check(fn: suspend (AppAuthUser) -> UserAggregate) =
      authUser?.let { fn(it) } ?: throw WonderlandError.NoAuth()

    return when (command) {
      is UserCommand.SuperCreate -> check { handle(it, command, cacheId) }

      is UserCommand.StartCreate -> handle(command, cacheId)
      is UserCommand.RefreshCreate -> handle(command, cacheId)
      is UserCommand.FinishCreate -> handle(command, cacheId)

      is UserCommand.StartActivateIdentifier -> check { handle(it, command, cacheId) }
      is UserCommand.RefreshActivateIdentifier -> check { handle(it, command, cacheId) }
      is UserCommand.FinishActivateIdentifier -> check { handle(it, command, cacheId) }
      is UserCommand.DeactivateIdentifier -> check { handle(it, command, cacheId) }

      is UserCommand.StartChangePassword -> check { handle(it, command, cacheId) }
      is UserCommand.RefreshChangePassword -> check { handle(it, command, cacheId) }
      is UserCommand.FinishChangePassword -> check { handle(it, command, cacheId) }

      is UserCommand.SuperUpdate -> check { handle(it, command, cacheId) }
      is UserCommand.UpdateBasicInfo -> check { handle(it, command, cacheId) }
      is UserCommand.Delete -> check { handle(it, command, cacheId) }
    }
  }

  override suspend fun introspect(token: String, cacheId: Long?): AppAuthUser {
    val principal = introspector.introspect(token).awaitSingle()
    val user = userRepository.getById(UserAggregate.Id(principal.name), cacheId)
    return AppAuthUser(principal, user)
  }
}
