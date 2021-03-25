package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserId
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError

sealed class Errors(override val message: String, override val cause: Throwable?) : AbstractError(message, cause) {
  data class IdentifierNotActivated(val userId: UserId, val value: String, override val cause: Throwable? = null) :
    Errors("User[$userId] with Identifier[$value] is not activated", cause)

  data class SpecificWayLoginFailed(val specificWay: Identifier.SpecificWay, override val cause: Throwable? = null) :
    Errors("Failed to login via SpecificWay[${specificWay.name}]", cause)
}
