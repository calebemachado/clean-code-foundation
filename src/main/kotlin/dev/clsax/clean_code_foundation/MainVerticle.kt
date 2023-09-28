package dev.clsax.clean_code_foundation

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dev.clsax.clean_code_foundation.before.AccountService
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
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
    val router = routes()

    router.route().handler { context ->
      val address = context.request().connection().remoteAddress().toString()
      val queryParams = context.queryParams()
      val name = queryParams.get("name") ?: "unknown"
      context.json(
        json {
          obj(
            "name" to name,
            "address" to address,
            "message" to "Hello $name connected from $address"
          )
        }
      )
    }

    val accountService = AccountService(vertx)

    val options = httpServerOptionsOf(idleTimeout = 5, idleTimeoutUnit = TimeUnit.MINUTES, logActivity = true)
    vertx.createHttpServer(options)
      .requestHandler(router)
      .listen(8888)
      .onComplete { println("HttpSever started at ${it.result().actualPort()}") }
      .await()
  }

  override suspend fun stop() {
    super.stop()
  }

  private suspend fun routes(): Router {
    val router = Router.router(vertx)

    router.get("/hello").handler { it.response().end("Hello from my route") }

    return router
  }
}
