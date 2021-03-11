package com.ukonnra.wonderland.doorknob.authentication

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.infrastructure.error.AbstractError

data class PreLoginModel(
  val csrfToken: String,
  val challenge: String,
  val error: AbstractError?,
)

data class LoginModel(
  val csrfToken: String,
  val challenge: String,
  val identifier: Identifier,
  val password: String,
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
