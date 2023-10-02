package dev.clsax.cleancodefoundation

import dev.clsax.cleancodefoundation.before.Input
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class AccountRouterTest {

  companion object {
    lateinit var client: WebClient

    @BeforeAll
    @JvmStatic
    internal fun setUp(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
      client = WebClient.create(vertx, WebClientOptions().setDefaultPort(123456))
      runBlocking(vertx.dispatcher()) {
        awaitResult<String> { vertx.deployVerticle(MainVerticle(), it) }
      }
    }

    @AfterAll
    @JvmStatic
    internal fun tearDown(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
      vertx.close()
    }
  }

  @Test
  fun `Deve criar um passageiro`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val input =
      Input(name = "John Doe", email = "john.doe${Math.random()}@gmail.com", cpf = "272.751.500-69", isPassenger = true)
    runBlocking(vertx.dispatcher()) {
      val postResult = awaitResult<HttpResponse<Buffer>> {
        client
          .post("/accounts")
          .send {
            json {
              obj(
                "name" to input.name,
                "email" to input.email,
                "cpf" to input.cpf,
                "isPassenger" to input.isPassenger
              )
            }
          }
      }
      Assertions.assertEquals(201, postResult.statusCode())
      val responseBody = postResult.bodyAsJsonObject()
      Assertions.assertNotNull(responseBody.getString("accountId"))

      val account = awaitResult<HttpResponse<Buffer>> {
        client.get("/accounts/${responseBody.getString("accountId")}")
      }.bodyAsJsonObject()

      Assertions.assertNotNull(account)
      Assertions.assertNotNull(account.getString("accountId"))
      Assertions.assertEquals(input.name, account.getString("name"))
    }
  }
}
