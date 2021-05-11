package com.ukonnra.wonderland.infrastructure.core.service

import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.AuthUser
import com.ukonnra.wonderland.infrastructure.core.Command
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError

interface AbstractAggregateService<ID : AggregateId, A : Aggregate<ID>, C : Command<ID, A>, AU : AuthUser<*>> :
  AbstractService {
  suspend fun introspect(token: String, cacheId: Long? = null): AU
  @Throws(AbstractError::class)
  suspend fun handle(authUser: AU?, command: C, cacheId: Long? = null): A
  @Throws(AbstractError::class)
  suspend fun handle(token: String?, command: C, cacheId: Long? = null): A =
    handle(token?.let { introspect(it) }, command, cacheId)
}
