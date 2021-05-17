package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError
import org.springframework.http.HttpStatus

sealed class Errors(override val message: String, override val cause: Throwable?) : AbstractError(message, cause) {
  data class IdentifierNotExist(
    val userId: UserAggregate.Id,
    val value: String,
  ) :
    Errors("User[${userId.value}] with Identifier[$value] does not exist", null)

  data class IdentifierAlreadyActivated(
    val userId: UserAggregate.Id,
    val value: String,
    override val cause: Throwable? = null
  ) :
    Errors("User[${userId.value}] with Identifier[$value] is not activated", cause)

  data class IdentifierNotRefreshable(
    val userId: UserAggregate.Id,
    val value: String,
    override val cause: Throwable? = null
  ) :
    Errors("Identifier[$value] of User[${userId.value}] is not refreshable", cause)

  data class IdentifierNotValid(
    val userId: UserAggregate.Id,
    val value: String,
    override val cause: Throwable? = null
  ) :
    Errors("Identifier[$value] of User[${userId.value}] is not valid, please refresh it again", cause)

  data class ActivateCodeNotMatch(
    val userId: UserAggregate.Id,
    val value: String,
  ) :
    Errors("Activate code for Identifier[$value] of User[${userId.value}] not match", null)

  data class SpecificWayLoginMissingParam(val specificWay: Identifier.SpecificWay, val param: String) :
    Errors("Failed to login via SpecificWay[${specificWay.name}] because Param[$param] is missing", null)

  data class SpecificWayNotActivated(val specificWay: String) :
    Errors("SpecificWay[$specificWay] is not activated", null) {
    constructor(specificWay: Identifier.SpecificWay) : this(specificWay.name)

    override val statusCode: Int
      get() = HttpStatus.INTERNAL_SERVER_ERROR.value()
  }
}
