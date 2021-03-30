package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserId
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError
import org.springframework.http.HttpStatus

sealed class Errors(override val message: String, override val cause: Throwable?) : AbstractError(message, cause) {
  data class IdentifierNotActivated(val userId: UserId, val value: String, override val cause: Throwable? = null) :
    Errors("User[$userId] with Identifier[$value] is not activated", cause)

  data class SpecificWayLoginMissingParam(val specificWay: Identifier.SpecificWay, val param: String) :
    Errors("Failed to login via SpecificWay[${specificWay.name}] because Param[$param] is missing", null)

  data class SpecificWayNotActivated(val specificWay: String) :
    Errors("SpecificWay[$specificWay] is not activated", null) {
    constructor(specificWay: Identifier.SpecificWay) : this(specificWay.name)

    override val statusCode: HttpStatus
      get() = HttpStatus.INTERNAL_SERVER_ERROR
  }
}
