package dev.clsax.cleancodefoundation

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dev.clsax.cleancodefoundation.before.AccountService
import dev.clsax.cleancodefoundation.before.Input
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.http.httpServerOptionsOf
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
    val router = Router.router(vertx)
    val accountService = AccountService(vertx)
    val accountRoutes = accountRoutes(router, accountService)

    val options = httpServerOptionsOf(idleTimeout = 5, idleTimeoutUnit = TimeUnit.MINUTES, logActivity = true)
    vertx.createHttpServer(options)
      .requestHandler(accountRoutes)
      .listen(8888)
      .onComplete { println("HttpSever started at ${it.result().actualPort()}") }
      .await()
  }

  override suspend fun stop() {
    super.stop()
  }

  private suspend fun accountRoutes(router: Router, accountService: AccountService): Router {
    router.post("/accounts")
      .consumes("application/json")
      .handler(BodyHandler.create())
      .coroutineHandler {
        val body = it.body().asJsonObject()
        val (name, email, cpf, isPassenger, carplate, isDriver) = body.mapTo(Input::class.java)
        val signUpResponse = accountService.signup(Input(name, email, cpf, isPassenger, carplate, isDriver))
        it.response()
          .putHeader("Location", "/accounts/${signUpResponse.accountId}")
          .setStatusCode(201)
          .end()
          .await()
      }

    return router
  }
}
