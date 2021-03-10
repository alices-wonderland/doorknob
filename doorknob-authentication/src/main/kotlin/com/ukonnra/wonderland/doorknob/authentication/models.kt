package com.ukonnra.wonderland.doorknob.authentication

import com.ukonnra.wonderland.infrastructure.error.AbstractError

data class PreLoginModel(
  val csrfToken: String,
  val challenge: String,
  val error: AbstractError?,
)

data class LoginModel(
  val csrfToken: String,
  val challenge: String,
  val identifier: String,
  val password: String,
  val type: Int,
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

data class DoorKnobUserModel(
  val id: String,
  val password: String,
)

sealed class IdentifierBehavior(open val identifier: Identifier) {
  data class ViaPassword(override val identifier: Identifier,  val password: String): IdentifierBehavior(identifier)
  data class ViaSpecificWay(override val identifier: Identifier, val specificWay: Identifier.SpecificWay): IdentifierBehavior(identifier)
}

data class Identifier(val type: Type, val value: String, val activated: Boolean = false) {
  enum class Type (val specificWays: List<SpecificWay>) {
    EMAIL(listOf(SpecificWay.EMAIL_SEND)),
    PHONE(listOf(SpecificWay.PHONE_CALL, SpecificWay.PHONE_SEND_MESSAGE)),
    GITHUB(listOf(SpecificWay.GITHUB_AUTH));
  }

  enum class SpecificWay {
    EMAIL_SEND, PHONE_CALL, PHONE_SEND_MESSAGE, GITHUB_AUTH;
  }
}
