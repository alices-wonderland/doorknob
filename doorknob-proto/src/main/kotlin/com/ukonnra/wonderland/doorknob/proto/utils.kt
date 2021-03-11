package com.ukonnra.wonderland.doorknob.proto

import com.google.protobuf.Timestamp
import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.Role
import com.ukonnra.wonderland.doorknob.core.domain.user.User
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.UserId
import com.ukonnra.wonderland.doorknob.core.domain.user.WonderlandService
import java.time.Instant

fun UserAggregateProto.toModel(): UserAggregate = UserAggregate(
  User(
    UserId(this.id),
    this.nickname,
    this.password,
    this.identifiersList.map { it.toModel() },
    this.createdAt.toInstant(),
    this.lastUpdatedAt.toInstant(),
    Role.valueOf(this.role.name),
    this.servicesEnabledList.map { WonderlandService.valueOf(it.name) }.toSet(),
    this.deleted,
  )
)

fun IdentifierProto.toModel(): Identifier =
  Identifier(Identifier.Type.valueOf(this.type.name), this.value, this.activated)

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
