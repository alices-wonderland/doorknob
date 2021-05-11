package com.ukonnra.wonderland.infrastructure.core.repository

import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateId

interface Repository<ID : AggregateId, A : Aggregate<ID>> {
  suspend fun saveAll(aggregates: List<A>, cacheId: Long? = null)
  suspend fun getById(targetId: ID, cacheId: Long? = null, evenDeleted: Boolean = false): A?
  suspend fun clearCache(cacheId: Long, withSaving: Boolean = true)

  suspend fun save(aggregate: A, cacheId: Long? = null) {
    saveAll(listOf(aggregate), cacheId)
  }
}
