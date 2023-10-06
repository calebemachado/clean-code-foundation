package dev.clsax.cleancodefoundation

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dev.clsax.cleancodefoundation.before.AccountService
import dev.clsax.cleancodefoundation.before.RideService
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.http.httpServerOptionsOf
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import java.util.concurrent.TimeUnit


class MainVerticle : CoroutineVerticle() {

  companion object {
    init {
      val objectMapper = DatabindCodec.mapper()
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
      objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
      val module = JavaTimeModule()
      objectMapper.registerModule(module)
    }
  }

  override suspend fun start() {
    val accountService = AccountService(vertx)
    val rideService = RideService(vertx)

    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create())
    defaultHandler(router)
    val accountRoutes = AccountRouter(router, accountService).routes()

    val options = httpServerOptionsOf(idleTimeout = 5, idleTimeoutUnit = TimeUnit.MINUTES, logActivity = true)
    vertx.createHttpServer(options)
      .requestHandler(accountRoutes)
      .listen(8888)
      .onComplete { println("HttpSever started at ${it.result().actualPort()}") }
      .await()
  }

  private fun defaultHandler(router: Router) {
    router.route().failureHandler {
      if (it.failure() is Exception) {
        it.response()
          .setStatusCode(404)
          .end(
            json {
              obj(
                "message" to "${it.failure().message}",
                "code" to "not_found"
              )
            }.toString()
          )
      }
    }

    router.get("/hello").handler { it.response().end("Hello from my route") }
  }

  override suspend fun stop() {
    super.stop()
  }
}
