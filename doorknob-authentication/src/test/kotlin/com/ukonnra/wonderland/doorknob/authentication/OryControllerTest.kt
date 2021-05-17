package com.ukonnra.wonderland.doorknob.authentication

import com.ukonnra.wonderland.doorknob.authentication.controller.OryController
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@SpringBootTest(classes = [OryController::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [ApplicationConfiguration::class])
@TestPropertySource("classpath:ory-controller.test.properties")
class OryControllerTest @Autowired constructor(
  private val authenticationService: AuthenticationService,
) {
  @LocalServerPort
  private var port = 0
  private lateinit var testClient: WebTestClient

  private val webClient = WebClient.builder().baseUrl("http://127.0.0.1:4444/oauth2/token")
    .defaultHeaders {
      it.contentType = MediaType.APPLICATION_FORM_URLENCODED
      it.setBasicAuth("absolem", "test-secret")
    }
    .build()

  @BeforeEach
  fun beforeEach() {
    runBlocking {
      testClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
      authenticationService.createClients()
    }
  }

  private suspend fun getToken(): String {
    return webClient.post()
      .body(BodyInserters.fromFormData("grant_type", "client_credentials").with("scope", "openid"))
      .awaitExchange<Map<String, String>> { it.awaitBody() }
      .getValue("access_token")
  }

  @Test
  fun testAccessUserInfo(): Unit = runBlocking {
    val token = getToken()
    val resp = testClient
      .get().uri("/user-info")
      .headers { it.setBearerAuth(token) }
      .exchange()
    resp.expectStatus().is2xxSuccessful
  }
}
