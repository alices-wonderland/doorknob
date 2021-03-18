package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ukonnra.wonderland.doorknob.core.domain.client.Client
import com.ukonnra.wonderland.doorknob.core.domain.client.ClientId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class SerdeTest {
  companion object {
    private val MAPPER = jacksonObjectMapper().findAndRegisterModules()
  }

  @Test
  fun testSerdeClients() {
    val map = mapOf(
      """
        {
          "@type" : "Frontend",
          "name" : "client-front",
          "scopes" : [ "scope1", "scope:sub1" ],
          "redirectUris" : [ "uri1" ],
          "id" : "id1",
          "meta" : {
            "skipConsent" : false
          }
        }
      """.trimIndent() to Client.Frontend("client-front", setOf("scope1", "scope:sub1"), setOf("uri1"), ClientId("id1")),
      """
        {
          "@type" : "Backend",
          "name" : "client-back",
          "scopes" : [ "scope1", "scope:sub1" ],
          "secret" : "secret",
          "id" : "id1",
          "meta" : {
            "skipConsent" : false
          }
        }
      """.trimIndent() to Client.Backend("client-back", setOf("scope1", "scope:sub1"), "secret", ClientId("id1")),
    )

    for ((expected, item) in map) {
      doCheck(item, expected)
    }
  }

  @Test
  fun testSerdeUser() {
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
      ),
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
