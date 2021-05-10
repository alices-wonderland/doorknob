package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

sealed class IdentifierBehavior(open val identifier: Identifier) {
  data class ViaPassword(override val identifier: Identifier, val password: String) : IdentifierBehavior(identifier)
  data class ViaSpecificWay(override val identifier: Identifier, val specificWay: Identifier.SpecificWay) :
    IdentifierBehavior(identifier)
}

data class Identifier(
  val type: Type,
  val value: String,
  val enableStatus: EnableStatus = EnableStatus.Disabled
) {
  val enabled: Boolean
    get() = enableStatus is EnableStatus.Enabled

  sealed class EnableStatus {
    object Disabled : EnableStatus()
    data class Hanging(
      val code: String = randomCode(),
      val createAt: Instant = Instant.now(),
    ) : EnableStatus() {
      @Suppress("UnusedPrivateMember")
      companion object {
        private const val REFRASHABLE_SECONDS = 1 * 60L
        private const val VALID_SECONDS = 10 * 60L
        private const val CODE_LENGTH = 6
        private val randomPool = ('A'..'Z') + ('0'..'9')
        private fun randomCode(): String = List(CODE_LENGTH) { randomPool.random() }.joinToString("")
      }

      val isRefreshable: Boolean = Instant.now().isAfter(createAt.plusSeconds(REFRASHABLE_SECONDS))
      val isValid: Boolean = Instant.now().isBefore(createAt.plusSeconds(VALID_SECONDS))

      fun refresh(): Hanging = copy(code = randomCode(), createAt = Instant.now())
    }

    object Enabled : EnableStatus()
  }

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
