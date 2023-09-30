package dev.clsax.cleancodefoundation.before

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Tuple
import java.time.LocalDateTime
import java.util.*

class AccountService(private val vertx: Vertx) {

  fun sendEmail(email: String, subject: String, message: String) {
    println("$email $subject $message")
  }

  suspend fun signup(input: Input): SignUpResponse {
    val connectOptions = PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("app")
      .setUser("postgres")
      .setPassword("123456")
    val poolOptions = PoolOptions()
      .setMaxSize(5)
    val client = PgPool.client(vertx, connectOptions, poolOptions)
    val accountId = UUID.randomUUID()
    try {
      val verificationCode = UUID.randomUUID()
      val date = LocalDateTime.now()
      val rows = client.preparedQuery("select * from cccat13.account where email = $1")
        .execute(Tuple.of(input.email))
        .await()
      val account = if (rows.iterator().hasNext()) {
        val next = rows.iterator().next()
        Account(
          next.getUUID("account_id"),
          next.getString("name")
        )
      } else {
        null
      }
      if (account != null) throw Exception("Account already exists")
      if (!input.name.matches(Regex("[a-zA-Z]+ [a-zA-Z]+"))) throw Exception("Invalid name")
      if (!input.email.matches(Regex("^(.+)@(.+)\$"))) throw Exception("Invalid email")
      if (!CpfValidator.validate(input.cpf)) throw Exception("Invalid cpf")
      if (input.isDriver && !input.carPlate.matches(Regex("[A-Z]{3}[0-9]{4}"))) throw Exception("Invalid plate")
      client.preparedQuery("insert into cccat13.account (account_id, name, email, cpf, car_plate, is_passenger, is_driver, date, is_verified, verification_code) values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)")
        .execute(
          Tuple.of(
            accountId,
            input.name,
            input.email,
            input.cpf,
            input.carPlate,
            !!input.isPassenger,
            !!input.isDriver,
            date,
            false,
            verificationCode
          )
        )
        .await()
      sendEmail(input.email, "Verification", "Please verify your code at first login $verificationCode")
    } finally {
      client.close()
    }
    return SignUpResponse(accountId)
  }

  suspend fun getAccount(accountId: UUID): Account? {
    val connectOptions = PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("app")
      .setUser("postgres")
      .setPassword("123456")
    val poolOptions = PoolOptions()
      .setMaxSize(5)
    val client = PgPool.client(vertx, connectOptions, poolOptions)
    val rows = client.preparedQuery("select account_id, name from cccat13.account where account_id = $1")
      .execute(Tuple.of(accountId))
      .await()
    return if (rows.iterator().hasNext()) {
      val next = rows.iterator().next()
      Account(
        next.getUUID("account_id"),
        next.getString("name")
      )
    } else null
  }
}
