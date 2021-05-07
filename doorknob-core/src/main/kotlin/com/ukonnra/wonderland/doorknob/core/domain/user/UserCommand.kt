package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.infrastructure.core.Command

sealed class UserCommand : Command<UserId> {

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
    override val targetId: UserId?
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
    override val targetId: UserId?
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
   * * Refresh the timestamp and enableCode
   */
  data class RefreshCreate(
    override val targetId: UserId,
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
   * * identifier.enableCode must equal to command.enableCode
   */
  data class FinishCreate(
    override val targetId: UserId,
    val enableCode: String,
    val nickname: String,
    val password: String,
  ) : UserCommand()

  /**
   * Update User:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be enabled
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Command must contain at least one field updated
   */
  data class Update(
    override val targetId: UserId,
    val nickname: String? = null,
    val password: String? = null,
  ) : UserCommand()

  /**
   * Start Enable User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be enabled
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
  data class StartEnableIdentifier(
    override val targetId: UserId,
    val identType: Identifier.Type,
    val identValue: String,
  ) : UserCommand()

  /**
   * Refresh Enable User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be enabled
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Target user must contain the same identType
   * * Instant.now() > identifier.createAt + REFRASHABLE_SECONDS
   *
   * **Result:**
   * * Refresh the timestamp and enableCode
   */
  data class RefreshEnableIdentifier(
    override val targetId: UserId,
    val identType: Identifier.Type,
  ) : UserCommand()

  /**
   * Finish Enable User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be enabled
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Target user must contain the same identType
   * * identifier.status must be HANGING
   * * Instant.now() < identifier.createAt + VALID_SECONDS
   * * identifier.enableCode must equal to command.enableCode
   */
  data class FinishEnableIdentifier(
    override val targetId: UserId,
    val identType: Identifier.Type,
    val enableCode: String,
  ) : UserCommand()

  /**
   *  Disable User Identifier:
   *
   * **Pre-requisites (handled in services):**
   * * Target user must exist
   * * Operator should exist and be enabled
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   * * Target user must contain the same identType
   * * identifier.status must be ENABLED
   */
  data class DisableIdentifier(
    override val targetId: UserId,
    val identType: Identifier.Type,
  ) : UserCommand()

  /**
   * Delete User:
   *
   * **Pre-requisites:**
   * * Target user must exist
   * * Operator should exist and be enabled
   *
   * **Business Logic:**
   * * Operator can only update self, or users whose role is strictly lower self
   */
  data class Delete(
    override val targetId: UserId,
  ) : UserCommand()
}
