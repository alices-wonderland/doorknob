package com.ukonnra.wonderland.infrastructure.core.error

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.FilterItem

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
sealed class WonderlandError(override val message: String, override val cause: Throwable?) : AbstractError
(message, cause) {
  data class NotFound(val type: String, val target: String, override val cause: Throwable? = null) :
    WonderlandError("$type[$target] is not found", cause) {
    constructor(type: String, target: AggregateId, cause: Throwable? = null) :
      this(type, target.value, cause)

    override val statusCode: Int
      get() = STATUS_NOT_FOUND
  }

  data class AlreadyDeleted(val type: String, val target: String, override val cause: Throwable? = null) :
    WonderlandError("$type[$target] already deleted", cause)

  data class AlreadyExists(val type: String, val target: String, override val cause: Throwable? = null) :
    WonderlandError("$type[$target] already exists", cause) {
    constructor(type: String, target: AggregateId, cause: Throwable? = null) :
      this(type, target.value, cause)

    override val statusCode: Int
      get() = STATUS_CONFLICT
  }

  data class NonNegativeParam(val paramName: String, override val cause: Throwable? = null) : WonderlandError
  ("`$paramName` should not be negative", cause)

  data class FieldNotSortable(val type: String, val field: String, override val cause: Throwable? = null) :
    WonderlandError("`$field` on $type is not sortable", cause) {
    override val statusCode: Int
      get() = STATUS_FORBIDDEN
  }

  data class InvalidFilterItem(val type: String, val filterItem: FilterItem, override val cause: Throwable? = null) :
    WonderlandError("$type cannot handle the filter item: $filterItem", cause) {
    override val statusCode: Int
      get() = STATUS_FORBIDDEN
  }

  data class NoAuth(val userId: AggregateId? = null, override val cause: Throwable? = null) :
    WonderlandError("User[${userId?.value}] has no auth to do the operation", cause) {
    override val statusCode: Int
      get() = STATUS_UNAUTHORIZED
  }

  data class UpdateNothing(val type: String, val target: String, override val cause: Throwable? = null) :
    WonderlandError("$type[$target] update nothing, the update command cannot be empty", cause) {
    constructor(type: String, target: AggregateId, cause: Throwable? = null) :
      this(type, target.value, cause)
  }

  data class InvalidCursor(val cursor: String, override val cause: Throwable? = null) :
    WonderlandError("Cursor[$cursor] is not valid", cause)

  data class UnknownAggregate(val type: String, override val cause: Throwable? = null) :
    WonderlandError("Aggregate[$type] is unknown", cause)
}
