package com.ukonnra.wonderland.doorknob.core.domain.user.impl

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.UserRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
@Suppress("MagicNumber")
class InMemoryUserRepository : UserRepository {
  internal val values: MutableMap<UserAggregate.Id, UserAggregate> = mutableMapOf()
  internal val cache: MutableMap<Long, MutableMap<UserAggregate.Id, UserAggregate>> = mutableMapOf()

  override suspend fun getByIdentifier(
    identType: Identifier.Type,
    identValue: String,
    withoutDeleted: Boolean,
    cacheId: Long?,
  ): UserAggregate? = coroutineScope {
    delay(500)

    fun doGetByIdentifier(
      map: Map<UserAggregate.Id, UserAggregate>,
    ): UserAggregate? = map.values.firstOrNull { value ->
      val identMatches = when (val data = value.userInfo) {
        is UserAggregate.UserInfo.Created -> data.identifiers.values.any {
          it.type == identType && it.value == identValue && (if (withoutDeleted) !value.deleted else true)
        }
        is UserAggregate.UserInfo.Uncreated -> data.identifier.type == identType && data.identifier.value == identValue
      }

      val deletedMatches = if (withoutDeleted) {
        !value.deleted
      } else {
        true
      }

      identMatches && deletedMatches
    }

    when (cacheId) {
      null -> doGetByIdentifier(values)
      else -> doGetByIdentifier(cache[cacheId] ?: emptyMap())
        ?: doGetByIdentifier(values)
    }
  }

  override suspend fun saveAll(aggregates: List<UserAggregate>, cacheId: Long?) = coroutineScope {
    delay(500)
    val target = when (cacheId) {
      null -> values
      else -> cache.getOrPut(cacheId) {
        mutableMapOf()
      }
    }
    for (item in aggregates) {
      target[item.id] = item
    }
  }

  override suspend fun getById(targetId: UserAggregate.Id, cacheId: Long?, evenDeleted: Boolean): UserAggregate? =
    coroutineScope {
      delay(500)
      (
        when (cacheId) {
          null -> values[targetId]
          else -> cache[cacheId]?.get(targetId) ?: values[targetId]
        }
        )?.let {
        if (evenDeleted || !it.deleted) {
          it
        } else {
          null
        }
      }
    }

  override suspend fun clearCache(cacheId: Long, withSaving: Boolean) {
    val items = cache.remove(cacheId) ?: emptyMap()
    if (withSaving) {
      for ((k, v) in items) {
        values[k] = v
      }
    }
  }
}
