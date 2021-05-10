package com.ukonnra.wonderland.infrastructure.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  JsonSubTypes.Type(value = Cursor.Date::class, name = "DATE"),
  JsonSubTypes.Type(value = Cursor.Num::class, name = "NUMBER"),
  JsonSubTypes.Type(value = Cursor.Str::class, name = "STR"),
)
sealed class Cursor {
  abstract val id: String
  abstract val value: Any

  data class Date(
    @JsonProperty("id") override val id: String,
    @JsonProperty("value") override val value: Instant
  ) : Cursor()

  data class Num(
    @JsonProperty("id") override val id: String,
    @JsonProperty("value") override val value: Number
  ) : Cursor()

  data class Str(
    @JsonProperty("id") override val id: String,
    @JsonProperty("value") override val value: String
  ) : Cursor()

  companion object {
    fun decode(mapper: ObjectMapper, value: String): Cursor = try {
      val jsonString = Base64.getUrlDecoder().decode(value).toString(StandardCharsets.UTF_8)
      mapper.readValue(jsonString, jacksonTypeRef())
    } catch (e: JsonProcessingException) {
      throw WonderlandError.InvalidCursor(value, e)
    }
  }

  fun encode(mapper: ObjectMapper): String = Base64.getUrlEncoder().encodeToString(mapper.writeValueAsBytes(this))
}
