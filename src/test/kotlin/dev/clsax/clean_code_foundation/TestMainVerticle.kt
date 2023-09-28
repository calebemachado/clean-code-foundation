package dev.clsax.clean_code_foundation

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  @Test
  fun start_http_server(vertx: Vertx, testContext: VertxTestContext) {
    vertx.createHttpServer()
      .requestHandler { handler -> handler.response().end() }
      .listen(16969)
      .onComplete(testContext.succeedingThenComplete())

    Assertions.assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    if (testContext.failed()) {
      throw testContext.causeOfFailure()
    }
  }

  @Test
  fun any_http_call_should_return_json(vertx: Vertx, testContext: VertxTestContext) {
    val client = vertx.createHttpClient()

    client
      .request(HttpMethod.GET, 8888, "localhost", "/")
      .compose { req -> req.send().compose { it.body() } }
      .onComplete { ar ->
        if (ar.succeeded()) {
          val buffer = ar.result()
          Assertions.assertEquals(buffer.toJson(), json {
            obj(
              "name" to "unknown",
              "address" to "127.0.0.1:62304",
              "message" to "Hello unknown connected from 127.0.0.1:62304"
            )
          })
        }
      }
    testContext.completeNow()
    if (testContext.failed()) {
      throw testContext.causeOfFailure()
    }
  }
}
