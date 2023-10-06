package dev.clsax.cleancodefoundation

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.time.LocalDateTime
import java.util.*

class AccountDAODatabase(private val vertx: Vertx) : AccountDAO {

  companion object {
    fun getSqlClient(vertx: Vertx): SqlClient {
      val connectOptions = PgConnectOptions()
        .setPort(5432)
        .setHost("localhost")
        .setDatabase("app")
        .setUser("postgres")
        .setPassword("123456")
      val poolOptions = PoolOptions()
        .setMaxSize(5)
      return PgPool.client(vertx, connectOptions, poolOptions)
    }
  }

  override suspend fun save(
    name: String,
    email: String,
    cpf: String,
    carPlate: String,
    isPassenger: Boolean,
    isDriver: Boolean,
    verificationCode: UUID
  ): UUID {
    val client = getSqlClient(vertx)
    val accountId = UUID.randomUUID()
    val date = LocalDateTime.now()
    client.preparedQuery(
      "insert into cccat13.account " +
        "(account_id, name, email, cpf, car_plate, is_passenger, is_driver, date, is_verified, verification_code) " +
        "values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)"
    )
      .execute(
        Tuple.of(accountId, name, email, cpf, carPlate, isPassenger, isDriver, date, false, verificationCode)
      )
      .await()
    client.close()
    return accountId
  }

  override suspend fun getAccountById(accountId: UUID): Account? {
    val client = getSqlClient(vertx)
    val rows = client.preparedQuery("select * from cccat13.account where account_id = $1")
      .execute(Tuple.of(accountId))
      .await()
    client.close()
    return DatabaseMapperUtils.buildAccountFromDatabase(rows)
  }

  override suspend fun getAccountByEmail(accountEmail: String): Account? {
    val client = getSqlClient(vertx)
    val rows = client.preparedQuery("select * from cccat13.account where email = $1")
      .execute(Tuple.of(accountEmail))
      .await()
    client.close()
    return DatabaseMapperUtils.buildAccountFromDatabase(rows)
  }

}
