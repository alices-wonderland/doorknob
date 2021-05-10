package com.ukonnra.wonderland.infrastructure.core.viewstore

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.Cursor
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import java.time.Instant
import java.time.format.DateTimeParseException

private fun <ID : AggregateId, A : Aggregate<ID>> A.toCursor(mapper: ObjectMapper, field: String): Cursor =
  when (val value = mapper.convertValue<Map<String, Any>>(this)[field]) {
    is String ->
      try {
        Cursor.Date(id.value, Instant.parse(value))
      } catch (e: DateTimeParseException) {
        Cursor.Str(id.value, value)
      }
    is Number -> Cursor.Num(id.value, value)
    else -> throw WonderlandError.FieldNotSortable(this.javaClass.simpleName, field)
  }

data class CursorEdge<ID : AggregateId, A : Aggregate<ID>>(val node: A, val cursor: Cursor) {
  constructor(mapper: ObjectMapper, node: A, field: String) : this(node, node.toCursor(mapper, field))
}
