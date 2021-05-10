package com.ukonnra.wonderland.infrastructure.core.locker

import org.redisson.api.RedissonClient
import kotlinx.coroutines.future.await as awaitKt

class RedissonLocker(private val redisson: RedissonClient) : AbstractLocker() {
  override suspend fun lock(vararg path: String, lockId: Long) {
    redisson.getLock(path.joinToString(".")).lockAsync(lockId).awaitKt()
  }

  override suspend fun unlock(vararg path: String, lockId: Long) {
    val lock = redisson.getLock(path.joinToString("."))
    if (lock.isHeldByThread(lockId)) {
      lock.unlockAsync(lockId).awaitKt()
    }
  }
}
