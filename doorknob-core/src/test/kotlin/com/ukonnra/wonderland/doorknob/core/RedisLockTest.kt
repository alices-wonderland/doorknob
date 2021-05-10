package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.Role
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.core.domain.user.UserCommand
import com.ukonnra.wonderland.doorknob.core.domain.user.UserRepository
import com.ukonnra.wonderland.doorknob.core.domain.user.impl.InMemoryUserRepository
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@SpringBootTest
@ContextConfiguration(classes = [TestApplicationConfiguration::class])
@TestPropertySource("classpath:redisson-lock.test.properties")
class RedisLockTest @Autowired constructor(
  private val transactionService: TransactionService,
  private val userRepository: UserRepository,
  private val introspector: ReactiveOpaqueTokenIntrospector,
) {
  private val webClient = WebClient.builder().baseUrl("http://127.0.0.1:4444/oauth2/token")
    .defaultHeaders {
      it.contentType = MediaType.APPLICATION_FORM_URLENCODED
      it.setBasicAuth("absolem", "test-secret")
    }
    .build()

  @BeforeEach
  fun beforeEach() {
    (userRepository as InMemoryUserRepository).apply {
      values.clear()
      cache.clear()
    }
  }

  private suspend fun getPrincipal(): OAuth2AuthenticatedPrincipal {
    val token = webClient.post()
      .body(BodyInserters.fromFormData("grant_type", "client_credentials").with("scope", "openid"))
      .awaitExchange<Map<String, String>> { it.awaitBody() }
      .getValue("access_token")
    return introspector.introspect(token).awaitSingle()
  }

  @Test
  fun testTransaction() = runBlocking {
    val commands = listOf(
      UserCommand.SuperCreate(
        "nickname",
        "password",
        Identifier(Identifier.Type.EMAIL, "email@email.com", Identifier.EnableStatus.Enabled),
        Role.USER,
      )
    )
    val results = transactionService.handleCommands(getPrincipal(), commands)
    print(results)
  }

  @Test
  fun testTransaction_duplicate() = runBlocking {
    val commands = listOf(
      UserCommand.SuperCreate(
        "nickname",
        "password",
        Identifier(Identifier.Type.EMAIL, "email@email.com", Identifier.EnableStatus.Enabled),
        Role.USER,
      ),
      UserCommand.SuperCreate(
        "nickname",
        "password",
        Identifier(Identifier.Type.EMAIL, "email@email.com", Identifier.EnableStatus.Enabled),
        Role.USER,
      )
    )
    val principal = getPrincipal()

    val ex = Assertions.assertThrows(WonderlandError.AlreadyExists::class.java) {
      runBlocking {
        transactionService.handleCommands(principal, commands)
      }
    }
    Assertions.assertEquals(WonderlandError.AlreadyExists("DoorKnob:User", "email@email.com"), ex)
  }

  @Test
  fun testTransaction_parallel() = runBlocking {
    val commands = listOf(
      UserCommand.SuperCreate(
        "nickname",
        "password",
        Identifier(Identifier.Type.EMAIL, "email@email.com", Identifier.EnableStatus.Enabled),
        Role.USER,
      )
    )
    val principal = getPrincipal()

    val createResults =
      transactionService.handleCommands(principal, commands).filterIsInstance<UserAggregate>()
    Assertions.assertEquals(1, createResults.size)
    val createResult = createResults.first()

    val update1Result = async {
      val cs = listOf(
        UserCommand.Update(
          createResult.id,
          nickname = "new_nickname"
        )
      )
      transactionService.handleCommands(principal, cs).filterIsInstance<UserAggregate>()
        .first().data as UserAggregate.Data.Created
    }

    val update2Result = async {
      val cs = listOf(
        UserCommand.Update(
          createResult.id,
          password = "new_password"
        )
      )
      transactionService.handleCommands(principal, cs).filterIsInstance<UserAggregate>()
        .first().data as UserAggregate.Data.Created
    }

    val updateResult = awaitAll(update1Result, update2Result)
    Assertions.assertEquals("new_nickname", updateResult[0].nickname)
    Assertions.assertTrue(
      updateResult[0].lastUpdatedAt
        .isAfter((createResult.data as UserAggregate.Data.Created).lastUpdatedAt)
    )

    Assertions.assertEquals("new_nickname", updateResult[1].nickname)
    Assertions.assertEquals("new_password", updateResult[1].password)
    Assertions.assertTrue(updateResult[1].lastUpdatedAt.isAfter(updateResult[0].lastUpdatedAt))

    val newItem = userRepository.getById(createResult.id)!!.data as UserAggregate.Data.Created
    Assertions.assertEquals(updateResult[1], newItem)
  }
}
