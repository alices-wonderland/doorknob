package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.infrastructure.core.repository.Repository

interface UserRepository : Repository<UserAggregate.Id, UserAggregate> {
  suspend fun getByIdentifier(
    identType: Identifier.Type,
    identValue: String,
    withoutDeleted: Boolean = true,
    cacheId: Long? = null,
  ): UserAggregate?
}
