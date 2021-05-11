package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.doorknob.core.AppAuthUser
import com.ukonnra.wonderland.doorknob.core.Errors
import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateCompanion
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import java.time.Instant
import java.util.UUID

@Suppress("ThrowsCount")
data class UserAggregate(
  val userInfo: UserInfo,
  override val id: Id = Id(),
  val deleted: Boolean = false,
  val createdAt: Instant = Instant.now(),
) : Aggregate<UserAggregate.Id> {
  companion object : AggregateCompanion {
    override val type: String
      get() = "DoorKnob:User"

    fun handle(command: UserCommand.StartCreate): UserAggregate {
      val identifier = Identifier.Hanging(command.identType, command.identValue)
      return UserAggregate(UserInfo.Uncreated(identifier))
    }

    fun handle(operator: AppAuthUser, command: UserCommand.SuperCreate): UserAggregate {
      when {
        !operator.hasRole(Role.ADMIN) || !operator.hasRole(command.role, true) ->
          throw WonderlandError.NoAuth(operator.id)
      }

      return UserAggregate(
        UserInfo.Created(
          command.nickname,
          command.password,
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
    data class Uncreated(val identifier: Identifier) : UserInfo()
    data class Created(
      val nickname: String,
      val password: String,
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
      override val createAt: Instant = Instant.now(),
      override val code: String = Activatable.randomCode(),
    ) :
      Password(), Activatable<Hanging> {
      override fun refresh(): Hanging = copy(createAt = Instant.now(), code = Activatable.randomCode())
    }
  }

  fun handle(command: UserCommand.RefreshCreate): UserAggregate {
    val data = userInfo as? UserInfo.Uncreated ?: throw WonderlandError.AlreadyExists(type, id)

    val identifier = data.identifier as? Identifier.Hanging
      ?: throw Errors.IdentifierAlreadyActivated(id, data.identifier.value)

    if (!identifier.isRefreshable) {
      throw Errors.IdentifierNotRefreshable(id, data.identifier.value)
    }

    return copy(userInfo = data.copy(identifier = data.identifier.refresh()))
  }

  fun handle(command: UserCommand.FinishCreate): UserAggregate {
    val data = userInfo as? UserInfo.Uncreated ?: throw WonderlandError.AlreadyExists(type, id)

    val identifier = data.identifier as? Identifier.Hanging
      ?: throw Errors.IdentifierAlreadyActivated(id, data.identifier.value)

    if (!identifier.isValid) {
      throw Errors.IdentifierNotValid(id, data.identifier.value)
    }

    if (identifier.code != command.code) {
      throw Errors.EnableCodeNotMatch(id, data.identifier.value)
    }

    return copy(
      userInfo = UserInfo.Created(
        command.nickname,
        command.password,
        mapOf(identifier.type to Identifier.Activated(identifier))
      )
    )
  }

  @Suppress("ThrowsCount")
  fun handle(operator: AppAuthUser, command: UserCommand.Update): UserAggregate {
    fun isUpdateNothing(): Boolean = command.nickname == null

    val data = when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      operator.id != id && !operator.hasRole(userInfo.role, true) -> throw WonderlandError.NoAuth(operator.id)
      isUpdateNothing() -> throw WonderlandError.UpdateNothing(type, id)
      else -> userInfo
    }

    return copy(
      userInfo = data.copy(
        nickname = command.nickname ?: data.nickname,
        lastUpdatedAt = Instant.now(),
      )
    )
  }

  fun handleDelete(operator: AppAuthUser): UserAggregate {
    when {
      userInfo !is UserInfo.Created -> throw WonderlandError.NotFound(type, id)
      operator.id != id && !operator.hasRole(userInfo.role, true) -> throw WonderlandError.NoAuth(operator.id)
    }

    return copy(deleted = true)
  }
}
