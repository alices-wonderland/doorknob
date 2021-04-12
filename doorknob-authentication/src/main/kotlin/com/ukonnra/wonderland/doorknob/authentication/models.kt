package com.ukonnra.wonderland.doorknob.authentication

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError
import java.util.Base64

data class PreLoginModel(
  val csrfToken: String,
  val challenge: String,
  val error: AbstractError?,
)

data class LoginModel(
  val csrfToken: String,
  val challenge: String,
  val identType: Identifier.Type,
  val identValue: String,
  val remember: Boolean,
)

data class PreConsentModel(
  val csrfToken: String,
  val challenge: String,
  val requestedScope: List<String>,
  val user: String,
  val client: String,
)

data class ConsentModel(
  val csrfToken: String,
  val user: String,
  val challenge: String,
  val accept: Boolean,
  val grantScopes: List<String>,
  val requestedAccessTokenAudience: List<String>,
  val remember: Boolean,
)

data class SpecificWayLoginMeta(
  val challenge: String,
  val csrfToken: String,
) {
  companion object {
    fun decode(value: String): SpecificWayLoginMeta =
      Base64.getUrlDecoder().decode(value).let { jacksonObjectMapper().readValue(it, jacksonTypeRef()) }
  }

  fun encode(): String = jacksonObjectMapper().writeValueAsBytes(this).let { Base64.getUrlEncoder().encodeToString(it) }
}
