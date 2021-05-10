package com.ukonnra.wonderland.infrastructure.core.locker

import java.util.Random

abstract class AbstractLocker {
  private val random = Random()

  internal abstract suspend fun lock(vararg path: String, lockId: Long)
  internal abstract suspend fun unlock(vararg path: String, lockId: Long)
  suspend fun <V> withLock(vararg path: String, action: suspend (Long) -> V): V {
    val realId = random.nextLong()
    return try {
      lock(*path, lockId = realId)
      action(realId)
    } finally {
      unlock(*path, lockId = realId)
    }
  }
}
