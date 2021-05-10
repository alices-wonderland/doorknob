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
  val data: Data,
  override val id: Id = Id(),
  val deleted: Boolean = false,
  val createdAt: Instant = Instant.now(),
) : Aggregate<UserAggregate.Id> {
  companion object : AggregateCompanion {
    override val type: String
      get() = "DoorKnob:User"

    fun handle(command: UserCommand.StartCreate): UserAggregate {
      val identifier = Identifier(command.identType, command.identValue, Identifier.EnableStatus.Hanging())
      return UserAggregate(Data.Uncreated(identifier))
    }

    fun handle(operator: AppAuthUser, command: UserCommand.SuperCreate): UserAggregate {
      when {
        !operator.hasRole(Role.ADMIN) || !operator.hasRole(command.role, true) ->
          throw WonderlandError.NoAuth(operator.id)
      }

      return UserAggregate(
        Data.Created(
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
    get() = when (data) {
      is Data.Created -> data.identifiers.isNotEmpty() &&
        data.identifiers.values.all { it.enableStatus is Identifier.EnableStatus.Enabled }
      else -> false
    }

  sealed class Data {
    data class Uncreated(val identifier: Identifier) : Data()
    data class Created(
      val nickname: String,
      val password: String,
      val identifiers: Map<Identifier.Type, Identifier> = emptyMap(),
      val role: Role = Role.USER,
      val lastUpdatedAt: Instant = Instant.now(),
      val servicesEnabled: Set<WonderlandService> = emptySet(),
    ) : Data()
  }

  fun handleRefreshCreate(): UserAggregate {
    val data = data as? Data.Uncreated ?: throw Errors.UserAlreadyCreated(id)

    val status = data.identifier.enableStatus as? Identifier.EnableStatus.Hanging
      ?: throw Errors.IdentifierAlreadyActivated(id, data.identifier.value)

    if (!status.isRefreshable) {
      throw Errors.IdentifierNotRefreshable(id, data.identifier.value)
    }

    return copy(data = data.copy(identifier = data.identifier.copy(enableStatus = status.refresh())))
  }

  fun handle(command: UserCommand.FinishCreate): UserAggregate {
    val data = data as? Data.Uncreated ?: throw Errors.UserAlreadyCreated(id)

    val status = data.identifier.enableStatus as? Identifier.EnableStatus.Hanging
      ?: throw Errors.IdentifierAlreadyActivated(id, data.identifier.value)

    if (!status.isValid) {
      throw Errors.IdentifierNotValid(id, data.identifier.value)
    }

    if (status.code != command.enableCode) {
      throw Errors.EnableCodeNotMatch(id, data.identifier.value)
    }

    val identifier = data.identifier.copy(enableStatus = Identifier.EnableStatus.Enabled)

    return copy(data = Data.Created(command.nickname, command.password, mapOf(identifier.type to identifier)))
  }

  @Suppress("ThrowsCount")
  fun handle(operator: AppAuthUser, command: UserCommand.Update): UserAggregate {
    fun isUpdateNothing(): Boolean = command.nickname == null && command.password == null

    val data = when {
      data !is Data.Created -> throw WonderlandError.NotFound(type, id.value)
      operator.id != id && !operator.hasRole(data.role, true) -> throw WonderlandError.NoAuth(operator.id)
      isUpdateNothing() -> throw WonderlandError.UpdateNothing(type, this.id.value)
      else -> data
    }

    return copy(
      data = data.copy(
        nickname = command.nickname ?: data.nickname,
        password = command.password ?: data.password,
        lastUpdatedAt = Instant.now(),
      )
    )
  }

  fun handleDelete(operator: AppAuthUser): UserAggregate {
    when {
      data !is Data.Created -> throw WonderlandError.NotFound(type, id.value)
      operator.id != id && !operator.hasRole(data.role, true) -> throw WonderlandError.NoAuth(operator.id)
    }

    return copy(deleted = true)
  }
}
