package com.ukonnra.wonderland.infrastructure.core

import kotlin.reflect.KClass

interface Command<ID : AggregateId, A : Aggregate<ID>> {
  val type: String
  val targetId: ID?
  val aggregateClass: KClass<A>
}
