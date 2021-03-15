package com.ukonnra.wonderland.doorknob.core.domain.user.impl

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.UserId
import com.ukonnra.wonderland.doorknob.core.domain.user.UserRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Repository
class InMemoryUserRepository(private val values: MutableMap<UserId, UserAggregate>) : UserRepository {
  override fun getByIdentifier(identType: Identifier.Type, identValue: String): Mono<UserAggregate?> =
    values.values.toFlux()
      .filter { (value) -> value.identifiers.any { it.type == identType && it.value == identValue } }
      .next()

  override fun getById(targetId: UserId): Mono<UserAggregate?> = Mono.justOrEmpty(values[targetId])

  override fun saveAll(aggregates: List<UserAggregate>): Mono<Void> = aggregates.map { it.value.id to it }.toMap().let {
    values.putAll(it)
    Mono.empty()
  }
}
