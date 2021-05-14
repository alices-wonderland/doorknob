package com.ukonnra.wonderland.infrastructure.testsuite

import kotlin.reflect.KClass

sealed class TestTask<A, C> {
  abstract val authUser: A?
  abstract val command: C

  data class Success<A, C, Ag>(
    override val authUser: A?,
    override val command: C,
    val checker: (C, Ag) -> Unit,
  ) : TestTask<A, C>()

  data class Failure<A, C, E : Throwable>(
    override val authUser: A?,
    override val command: C,
    val ex: KClass<E>,
    val checker: ((E) -> Unit)? = null,
  ) : TestTask<A, C>()
}
