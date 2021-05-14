package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.doorknob.core.AppAuthScope
import com.ukonnra.wonderland.doorknob.core.AppAuthUser
import com.ukonnra.wonderland.doorknob.core.Errors
import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateCompanion
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import java.time.Instant
import java.util.UUID

@Suppress("ThrowsCount", "TooManyFunctions")
data class UserAggregate(
  val userInfo: UserInfo,
  override val id: Id = Id(),
  val deleted: Boolean = false,
  val createdAt: Instant = Instant.now(),
) : Aggregate<UserAggregate.Id> {
  companion object : AggregateCompanion {
    override val type: String
      get() = "DoorKnob:Users"

    fun uncreated(identType: Identifier.Type, identValue: String) =
      UserAggregate(UserInfo.Uncreated(Identifier.Hanging(identType, identValue)))

    fun handle(command: UserCommand.StartCreate): Pair<UserAggregate, Identifier> {
      val identifier = Identifier.Hanging(command.identType, command.identValue)
      return UserAggregate(UserInfo.Uncreated(identifier)) to identifier
    }

    fun handle(authUser: AppAuthUser, command: UserCommand.SuperCreate): UserAggregate {
      when {
        !authUser.hasRole(Role.ADMIN) || !authUser.hasRole(command.role, true) ->
          throw WonderlandError.NoAuth(authUser.id)
        !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      }

      return UserAggregate(
        UserInfo.Created(
          command.nickname,
          Password.Normal(command.password),
          mapOf(command.identifier.type to command.identifier),
          command.role,
        )
      )
    }
  }

  data class Id(override val value: String) : AggregateId {
    constructor() : this(UUID.randomUUID().toString())
  }

  override val companion: AggregateCompanion
    get() = UserAggregate

  val enabled: Boolean
    get() = when (userInfo) {
      is UserInfo.Created -> userInfo.identifiers.isNotEmpty() &&
        userInfo.identifiers.values.all { it is Identifier.Activated }
      else -> false
    }

  sealed class UserInfo {
    data class Uncreated(val identifier: Identifier.Hanging) : UserInfo()
    data class Created(
      val nickname: String,
      val password: Password,
      val identifiers: Map<Identifier.Type, Identifier> = emptyMap(),
      val role: Role = Role.USER,
      val lastUpdatedAt: Instant = Instant.now(),
      val servicesEnabled: Set<WonderlandService> = emptySet(),
    ) : UserInfo()
  }

  sealed class Password {
    abstract val value: String

    data class Normal(override val value: String) : Password()
    data class Hanging(
      override val value: String,
      val identType: Identifier.Type,
      override val createAt: Instant = Instant.now(),
      override val code: String = Activatable.randomCode(),
    ) :
      Password(), Activatable<Hanging> {
      override fun refresh(): Hanging = copy(createAt = Instant.now(), code = Activatable.randomCode())
    }
  }

  fun refreshCreate(): Pair<UserAggregate, Identifier> {
    val data = userInfo as? UserInfo.Uncreated ?: throw WonderlandError.AlreadyExists(type, id)

    if (!data.identifier.isRefreshable) {
      throw Errors.IdentifierNotRefreshable(id, data.identifier.value)
    }

    val newIdent = data.identifier.refresh()

    return copy(userInfo = data.copy(identifier = newIdent)) to newIdent
  }

  fun finishCreate(code: String, nickname: String, password: String): UserAggregate {
    val data = userInfo as? UserInfo.Uncreated ?: throw WonderlandError.AlreadyExists(type, id)

    if (!data.identifier.isValid) {
      throw Errors.IdentifierNotValid(id, data.identifier.value)
    }

    if (data.identifier.code != code) {
      throw Errors.EnableCodeNotMatch(id, data.identifier.value)
    }

    return copy(
      userInfo = UserInfo.Created(
        nickname,
        Password.Normal(password),
        mapOf(data.identifier.type to Identifier.Activated(data.identifier))
      )
    )
  }

  fun updateBasicInfo(authUser: AppAuthUser, nickname: String?): UserAggregate {
    fun isUpdateNothing(): Boolean = nickname == null

    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      isUpdateNothing() -> throw WonderlandError.UpdateNothing(type, id)
      else -> userInfo
    }

    return copy(
      userInfo = data.copy(
        nickname = nickname ?: data.nickname,
        lastUpdatedAt = Instant.now(),
      )
    )
  }

  fun startChangePassword(authUser: AppAuthUser, identType: Identifier.Type): Pair<UserAggregate, Identifier> {
    val userInfo = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      authUser.id != id -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo
    }

    val ident = userInfo.identifiers[identType] ?: throw Errors.IdentifierNotExist(id, identType.name)

    val newValue = when (val pass = userInfo.password) {
      is Password.Hanging -> pass.refresh()
      is Password.Normal -> Password.Hanging(pass.value, identType)
    }

    return copy(userInfo = userInfo.copy(password = newValue)) to ident
  }

  fun refreshChangePassword(authUser: AppAuthUser): Pair<UserAggregate, Identifier> {
    val userInfo = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      authUser.id != id -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo
    }

    val (newValue, ident) = when (val pass = userInfo.password) {
      is Password.Hanging -> pass.refresh() to
        (userInfo.identifiers[pass.identType] ?: throw Errors.IdentifierNotExist(id, pass.identType.name))
      is Password.Normal -> throw WonderlandError.StateNotSuitable(type, id.value, "password")
    }

    return copy(userInfo = userInfo.copy(password = newValue)) to ident
  }

  fun finishChangePassword(authUser: AppAuthUser, code: String, password: String): UserAggregate {
    val userInfo = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      authUser.id != id -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      userInfo.password !is Password.Hanging -> throw WonderlandError.StateNotSuitable(type, id.value, "password")
      !userInfo.password.isValid -> throw Errors.IdentifierNotValid(id, "password")
      userInfo.password.code != code -> throw Errors.EnableCodeNotMatch(id, "password")
      else -> userInfo
    }

    return copy(userInfo = userInfo.copy(password = Password.Normal(password)))
  }

  @Suppress("ComplexMethod")
  fun superUpdate(
    authUser: AppAuthUser,
    nickname: String?,
    password: String?,
    identifiers: Map<Identifier.Type, String>?,
    role: Role?
  ): UserAggregate {
    fun isUpdateNothing(): Boolean = nickname == null && password == null && identifiers == null && role == null

    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !authUser.hasRole(Role.ADMIN) -> throw WonderlandError.NoAuth(authUser.id)
      role != null && !authUser.hasRole(role, true) -> throw WonderlandError.NoAuth(authUser.id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      isUpdateNothing() -> throw WonderlandError.UpdateNothing(type, id)
      else -> userInfo
    }

    val identifiersDelta = identifiers?.map { (k, v) -> k to Identifier.Activated(k, v) }?.toMap() ?: emptyMap()

    return copy(
      userInfo = data.copy(
        nickname = nickname ?: data.nickname,
        password = password?.let { Password.Normal(it) } ?: data.password,
        identifiers = data.identifiers + identifiersDelta,
        role = role ?: data.role,
        lastUpdatedAt = Instant.now(),
      )
    )
  }

  fun startActivateIdentifier(authUser: AppAuthUser, identType: Identifier.Type, identValue: String): UserAggregate {
    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo
    }

    val newIdentifier = Identifier.Hanging(identType, identValue)

    return copy(userInfo = data.copy(identifiers = data.identifiers + (newIdentifier.type to newIdentifier)))
  }

  fun refreshActivateIdentifier(authUser: AppAuthUser, identType: Identifier.Type): UserAggregate {
    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo
    }

    val newIdentifier = when (val ident = data.identifiers[identType]) {
      null -> throw Errors.IdentifierNotExist(id, identType.name)
      is Identifier.Hanging -> ident.refresh()
      else -> throw Errors.IdentifierNotRefreshable(id, identType.name)
    }

    return copy(userInfo = data.copy(identifiers = data.identifiers + (newIdentifier.type to newIdentifier)))
  }

  fun finishActivateIdentifier(authUser: AppAuthUser, identType: Identifier.Type, code: String): UserAggregate {
    val (data, ident) = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo to userInfo.identifiers[identType]
    }
    val newIdentifier = when {
      ident == null -> throw Errors.IdentifierNotExist(id, identType.name)
      ident !is Identifier.Hanging -> throw Errors.IdentifierAlreadyActivated(id, identType.name)
      !ident.isValid -> throw Errors.IdentifierNotValid(id, identType.name)
      ident.code != code -> throw Errors.EnableCodeNotMatch(id, identType.name)
      else -> Identifier.Activated(ident)
    }
    return copy(userInfo = data.copy(identifiers = data.identifiers + (newIdentifier.type to newIdentifier)))
  }

  fun deactivateIdentifier(authUser: AppAuthUser, identType: Identifier.Type): UserAggregate {
    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo
    }

    return copy(userInfo = data.copy(identifiers = data.identifiers - identType))
  }

  fun delete(authUser: AppAuthUser): UserAggregate {
    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      !isSelfOrAdminStrictlyHigher(authUser) -> throw WonderlandError.NoAuth(authUser.id)
      !authUser.hasScope(AppAuthScope.USERS_WRITE) -> throw WonderlandError.InvalidToken(authUser.id)
      else -> userInfo
    }

    return copy(deleted = true, userInfo = data.copy(identifiers = emptyMap()))
  }

  private fun isSelfOrAdminStrictlyHigher(authUser: AppAuthUser): Boolean {
    fun isAdminAndHigher() =
      userInfo is UserInfo.Created && authUser.hasRole(Role.ADMIN) && authUser.hasRole(userInfo.role, true)
    return authUser.id == id || isAdminAndHigher()
  }
}
