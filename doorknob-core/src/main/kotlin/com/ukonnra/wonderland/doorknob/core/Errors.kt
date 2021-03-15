package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.UserId
import com.ukonnra.wonderland.infrastructure.error.AbstractError

sealed class Errors(override val message: String) : AbstractError(message) {
  data class IdentifierNotActivated(val userId: UserId, val value: String) :
    Errors("User[$userId] with Identifier[$value] is not activated")
}
