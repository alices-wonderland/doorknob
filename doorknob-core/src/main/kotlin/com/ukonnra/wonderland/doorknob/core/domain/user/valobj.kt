package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.annotation.JsonIgnore

sealed class IdentifierBehavior(open val identifier: Identifier) {
  data class ViaPassword(override val identifier: Identifier, val password: String) : IdentifierBehavior(identifier)
  data class ViaSpecificWay(override val identifier: Identifier, val specificWay: Identifier.SpecificWay) :
    IdentifierBehavior(identifier)
}

data class Identifier(val type: Type, val value: String, val activated: Boolean = false) {
  enum class Type(@JsonIgnore val specificWays: List<SpecificWay>) {
    EMAIL(listOf(SpecificWay.EMAIL_SEND)),
    PHONE(listOf(SpecificWay.PHONE_CALL, SpecificWay.PHONE_SEND_MESSAGE)),
    GITHUB(listOf(SpecificWay.GITHUB_AUTH));
  }

  enum class SpecificWay(val urlFormat: String) {
    EMAIL_SEND("email-send"),
    PHONE_CALL("phone-call"),
    PHONE_SEND_MESSAGE("phone-send-message"),
    GITHUB_AUTH("github-auth");

    companion object {
      fun fromUrlFormat(urlFormat: String) = values().find { it.urlFormat == urlFormat }
    }
  }
}

enum class Role {
  USER, ADMIN, OWNER;
}

enum class WonderlandService {
  DOORKNOB, ABSOLEM;
}
