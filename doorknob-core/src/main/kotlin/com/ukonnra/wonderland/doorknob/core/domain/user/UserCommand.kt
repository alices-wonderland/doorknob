package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.infrastructure.core.Command

sealed class UserCommand : Command<UserId> {
  data class Create(
    val nickname: String,
    val password: String,
    val identifiers: List<Identifier>,
    val role: Role,
    val servicesEnabled: Set<WonderlandService>,
  ) : UserCommand() {
    override val targetId: UserId?
      get() = null
  }

  data class Update(
    override val targetId: UserId,
    val nickname: String?,
    val password: String?,
    val identifiers: List<Identifier>?,
    val role: Role?,
    val servicesEnabled: Set<WonderlandService>?,
  ) : UserCommand()

  data class Delete(
    override val targetId: UserId,
  ) : UserCommand()
}
