package com.ukonnra.wonderland.infrastructure.core.viewstore

import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateId

data class CursorConnection<ID : AggregateId, A : Aggregate<ID>>(
  val data: List<CursorEdge<ID, A>>,
  val hasPreviousPage: Boolean,
  val hasNextPage: Boolean
)
