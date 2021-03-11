package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class SerdeTest {
  companion object {
    private val MAPPER = jacksonObjectMapper().findAndRegisterModules()
  }

  @Test
  fun testSerde() {
    val map = mapOf(
      """
      {
        "id" : "id",
        "nickname" : "nickname",
        "password" : "password",
        "identifiers" : [ {
          "type" : "EMAIL",
          "value" : "email",
          "activated" : true
        } ],
        "createdAt" : 0.0,
        "lastUpdatedAt" : 0.0,
        "role" : "ADMIN",
        "servicesEnabled" : [ "ABSOLEM", "DOORKNOB" ],
        "deleted" : false,
        "activated" : true
      }
      """.trimIndent() to User(
        UserId("id"),
        "nickname",
        "password",
        listOf(Identifier(Identifier.Type.EMAIL, "email", true)),
        Instant.EPOCH,
        Instant.EPOCH,
        Role.ADMIN,
        setOf(WonderlandService.ABSOLEM, WonderlandService.DOORKNOB),
        false
      )
    )

    for ((expected, item) in map) {
      doCheck(item, expected)
    }
  }

  private inline fun <reified T> doCheck(value: T, expected: String) {
    val jsonStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value)
    Assertions.assertEquals(expected, jsonStr)

    val obj: T = MAPPER.readValue(jsonStr)
    Assertions.assertEquals(value, obj)
  }
}
