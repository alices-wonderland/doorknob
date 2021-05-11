package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.infrastructure.core.Command
import kotlin.reflect.KClass

sealed class UserCommand : Command<UserAggregate.Id, UserAggregate> {
  override val aggregateClass: KClass<UserAggregate>
    get() = UserAggregate::class

  override val type: String
    get() = UserAggregate.type

  /**
   * Super Create User:
   *
   * **Pre-requisites (handled in services):**
   * * Target user with the same identifier should not exist
   *
   * **Business Logic:**
   * * operator.role >= Role.ADMIN
   * * operator.role > command.role
   */
  data class SuperCreate(
    val nickname: String,
    val password: String,
    val identifier: Identifier,
    val role: Role,
  ) : UserCommand() {
    override val targetId: UserAggregate.Id?
      get() = null
  }

  /**
   * Start Create User:
   *
   * **Pre-requisites (handled in services):**
   * * Target user with the same identifier should not exist
   * * Operator should not exist
   *
   * **Result:**
   * * Create an user
   * * Start the activate process
   */
  data class StartCreate(
    val identType: Identifier.Type,
    val identValue: String,
  ) : UserCommand() {
    override val targetId: UserAggregate.Id?
      get() = null
  }

  /**
   * Refresh Create User:
   *
   * **Pre-requisites (handled in services):**
   * * Operator should not exist
   *
   * **Business Logic:**
   * * Target user should be Uncreated
   * * Instant.now() > identifier.createAt + REFRASHABLE_SECONDS
   *
   * **Result:**
   * * Refresh the timestamp and ActivateCode
   */
  data class RefreshCreate(
    override val targetId: UserAggregate.Id,
  ) : UserCommand()

  /**
   * Finish Create User:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should not exist
   *
   * **Business Logic:**
   * * identifier.status must be HANGING
   * * Instant.now() < identifier.createAt + VALID_SECONDS
   * * identifier.ActivateCode must equal to command.ActivateCode
   */
  data class FinishCreate(
    override val targetId: UserAggregate.Id,
    val code: String,
    val nickname: String,
    val password: String,
  ) : UserCommand()

  /**
   * Update User:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be Activated
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Command must contain at least one field updated
   */
  data class Update(
    override val targetId: UserAggregate.Id,
    val nickname: String? = null,
  ) : UserCommand()

  data class StartChangePassword(
    override val targetId: UserAggregate.Id,
  ) : UserCommand()

  data class RefreshChangePassword(
    override val targetId: UserAggregate.Id,
  ) : UserCommand()

  data class FinishChangePassword(
    override val targetId: UserAggregate.Id,
    val code: String,
    val password: String,
  ) : UserCommand()

  data class SuperUpdate(
    override val targetId: UserAggregate.Id,
    val nickname: String?,
    val password: String?,
    val role: Role?,
  ) : UserCommand()

  /**
   * Start Activate User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be Activated
   *
   * **Business Logic:**
   * * User with the same identifier must not exist
   * * Operator can only update self, or users whose role is strictly lower self
   * * Target user should not contain the same identType
   *
   * **Result:**
   * * Update identifier.status to HANGING
   * * Start the activate process
   */
  data class StartActivateIdentifier(
    override val targetId: UserAggregate.Id,
    val identType: Identifier.Type,
    val identValue: String,
  ) : UserCommand()

  data class RefreshActivateIdentifier(
    override val targetId: UserAggregate.Id,
    val identType: Identifier.Type,
  ) : UserCommand()

  /**
   * Finish Activate User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be Activated
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Target user must contain the same identType
   * * identifier.status must be HANGING
   * * Instant.now() < identifier.createAt + VALID_SECONDS
   * * identifier.ActivateCode must equal to command.ActivateCode
   */
  data class FinishActivateIdentifier(
    override val targetId: UserAggregate.Id,
    val identType: Identifier.Type,
    val code: String,
  ) : UserCommand()

  /**
   *  Deactivate User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be activated
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Target user must contain the same identType
   * * identifier must be activated
   */
  data class DeactivateIdentifier(
    override val targetId: UserAggregate.Id,
    val identType: Identifier.Type,
  ) : UserCommand()

  /**
   * Delete User:
   *
   * **Pre-requisites:**
   * * Target user must exist
   * * Operator should exist and be Activated
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   */
  data class Delete(
    override val targetId: UserAggregate.Id,
  ) : UserCommand()
}
