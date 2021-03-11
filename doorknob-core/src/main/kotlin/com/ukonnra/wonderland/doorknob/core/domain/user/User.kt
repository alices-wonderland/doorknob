package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ukonnra.wonderland.annotations.AggregateRoot
import java.time.Instant

@AggregateRoot(service = "DoorKnob")
@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
  val id: UserId,
  val nickname: String,
  val password: String,
  val identifiers: List<Identifier>,
  val createdAt: Instant,
  val lastUpdatedAt: Instant,
  val role: Role,
  val servicesEnabled: Set<WonderlandService>,
  val deleted: Boolean,
) {
  val activated: Boolean
    get() = this.identifiers.all { it.activated }
}
