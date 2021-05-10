package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

sealed class IdentifierBehavior(open val identifier: Identifier) {
  data class ViaPassword(override val identifier: Identifier, val password: String) : IdentifierBehavior(identifier)
  data class ViaSpecificWay(override val identifier: Identifier, val specificWay: Identifier.SpecificWay) :
    IdentifierBehavior(identifier)
}

sealed class Identifier(
  open val type: Type,
  open val value: String,
) {
  data class Hanging(
    override val type: Type,
    override val value: String,
    override val code: String = Activatable.randomCode(),
    override val createAt: Instant = Instant.now(),
  ) : Identifier(type, value), Activatable<Hanging> {
    override fun refresh(): Hanging = copy(code = Activatable.randomCode(), createAt = Instant.now())
  }

  data class Activated(
    override val type: Type,
    override val value: String,
  ) : Identifier(type, value) {
    constructor(identifier: Hanging) : this(identifier.type, identifier.value)
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

interface Activatable<T : Activatable<T>> {
  val code: String
  val createAt: Instant
  fun refresh(): T

  companion object {
    private const val REFRASHABLE_SECONDS = 1 * 60L
    private const val VALID_SECONDS = 10 * 60L
    private const val CODE_LENGTH = 6
    private val randomPool = ('A'..'Z') + ('0'..'9')
    internal fun randomCode(): String = List(CODE_LENGTH) { randomPool.random() }.joinToString("")
  }

  val isRefreshable: Boolean
    get() = Instant.now().isAfter(createAt.plusSeconds(REFRASHABLE_SECONDS))

  val isValid: Boolean
    get() = Instant.now().isBefore(createAt.plusSeconds(VALID_SECONDS))
}
