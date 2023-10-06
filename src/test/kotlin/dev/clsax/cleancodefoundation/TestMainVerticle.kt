package dev.clsax.cleancodefoundation

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  @Test
  fun `Should start http server`(vertx: Vertx, testContext: VertxTestContext) {
    vertx.createHttpServer()
      .requestHandler { handler -> handler.response().end() }
      .listen(16969)
      .onComplete(testContext.succeedingThenComplete())

    Assertions.assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    if (testContext.failed()) {
      throw testContext.causeOfFailure()
    }
  }
}
