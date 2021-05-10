package com.ukonnra.wonderland.infrastructure.core.error

data class ExternalError(
  val code: String,
  val data: Map<String, Any> = emptyMap(),
  override val message: String = "<Unknown External Error>",
  override val statusCode: Int = STATUS_BAD_REQUEST,
  override val cause: Throwable? = null,
) : AbstractError(message, cause)
