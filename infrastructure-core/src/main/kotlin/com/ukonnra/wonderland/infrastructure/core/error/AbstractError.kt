package com.ukonnra.wonderland.infrastructure.core.error

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties("cause", "stackTrace", "suppressed", "localizedMessage")
abstract class AbstractError(override val message: String, override val cause: Throwable?) : RuntimeException
(message, cause) {
  companion object {
    internal const val STATUS_BAD_REQUEST = 400
    internal const val STATUS_UNAUTHORIZED = 401
    internal const val STATUS_FORBIDDEN = 403
    internal const val STATUS_NOT_FOUND = 404
    internal const val STATUS_CONFLICT = 409
  }

  open val statusCode: Int = STATUS_BAD_REQUEST
}
