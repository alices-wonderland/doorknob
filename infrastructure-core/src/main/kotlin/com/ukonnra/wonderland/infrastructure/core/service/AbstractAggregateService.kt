package com.ukonnra.wonderland.infrastructure.core.service

import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.AuthUser
import com.ukonnra.wonderland.infrastructure.core.Command
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError

interface AbstractAggregateService<ID : AggregateId, A : Aggregate<ID>, C : Command<ID, A>, AU : AuthUser<*>> :
  AbstractService {
  @Throws(AbstractError::class)
  suspend fun handle(operator: AU?, command: C, cacheId: Long? = null): A
}
