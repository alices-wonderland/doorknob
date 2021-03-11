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

  enum class SpecificWay {
    EMAIL_SEND, PHONE_CALL, PHONE_SEND_MESSAGE, GITHUB_AUTH;
  }
}

enum class Role {
  USER, ADMIN, OWNER;
}

enum class WonderlandService {
  DOORKNOB, ABSOLEM;
}
