package com.ukonnra.wonderland.infrastructure.core

interface AggregateId {
  val value: String
}

interface Aggregate<ID : AggregateId> {
  val id: ID
  val companion: AggregateCompanion
}

interface AggregateCompanion {
  val type: String
}
