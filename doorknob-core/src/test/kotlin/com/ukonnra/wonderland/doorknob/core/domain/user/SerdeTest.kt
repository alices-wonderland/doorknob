package com.ukonnra.wonderland.doorknob.core.domain.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ukonnra.wonderland.doorknob.core.domain.client.Client
import com.ukonnra.wonderland.doorknob.core.domain.client.ClientId
import com.ukonnra.wonderland.infrastructure.testsuite.SerdeTester
import com.ukonnra.wonderland.infrastructure.testsuite.check
import org.junit.jupiter.api.Test
import java.time.Instant

class SerdeTest : SerdeTester {
  override val mapper: ObjectMapper
    get() = jacksonObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDateFormat(StdDateFormat().withColonInTimeZone(true))

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
          "meta" : [ "SKIP_CONSENT" ]
        }
      """.trimIndent() to Client.Frontend(
        "client-front",
        setOf("scope1", "scope:sub1"),
        setOf("uri1"),
        ClientId("id1"),
        setOf(Client.MetaItem.SKIP_CONSENT)
      ),
      """
        {
          "@type" : "Backend",
          "name" : "client-back",
          "scopes" : [ "scope1", "scope:sub1" ],
          "secret" : "secret",
          "id" : "id1",
          "meta" : []
        }
      """.trimIndent() to Client.Backend("client-back", setOf("scope1", "scope:sub1"), "secret", ClientId("id1")),
    )

    for ((expected, item) in map) {
      check(item, expected)
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
        "createdAt" : "1970-01-01T00:00:00Z",
        "lastUpdatedAt" : "1970-01-01T00:00:00Z",
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
      check(item, expected)
    }
  }
}
