package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.infrastructure.core.repository.Repository
import reactor.core.publisher.Mono

interface UserRepository : Repository<UserAggregate, UserId> {
  fun getByIdentifier(
    identType: Identifier.Type,
    identValue: String,
    withoutDeleted: Boolean = true
  ): Mono<UserAggregate?>
}
