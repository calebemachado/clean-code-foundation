package dev.clsax.cleancodefoundation

import dev.clsax.cleancodefoundation.before.AccountService
import dev.clsax.cleancodefoundation.before.Input
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class AccountRouter(
  private val router: Router,
  private val accountService: AccountService,
) {

  suspend fun routes(): Router {
    router.post("/accounts")
      .consumes("application/json")
      .coroutineHandler {
        val (name, email, cpf, isPassenger, carplate, isDriver) = Json.decodeFromString<Input>(it.body().asString())
        val signUpResponse = accountService.signup(Input(name, email, cpf, isPassenger, carplate, isDriver))
        it.response()
          .putHeader("Location", "/accounts/${signUpResponse.accountId}")
          .setStatusCode(201)
          .end()
          .await()
      }

    router.get("/accounts/:accountId")
      .consumes("application/json")
      .produces("application/json")
      .coroutineHandler {
        val accountId = it.pathParam("accountId")
        val account = accountService.getAccount(UUID.fromString(accountId))
        if (account == null) {
          it.response().setStatusCode(404).end()
        }
        it.response().end(Json.encodeToString(account))
      }

    return router
  }
}
