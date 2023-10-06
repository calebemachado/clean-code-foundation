package dev.clsax.cleancodefoundation.before

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.time.LocalDateTime
import java.util.*

class AccountService(private val vertx: Vertx) {

  companion object {
    private const val VALID_LAT_LONG_REGEX =
      "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$"
  }

  fun sendEmail(email: String, subject: String, message: String) {
    println("$email $subject $message")
  }

  suspend fun signup(signupInput: SignupInput): SignupResponse {
    val client = getSqlClient()
    val accountId = UUID.randomUUID()
    try {
      val verificationCode = UUID.randomUUID()
      val date = LocalDateTime.now()
      val rows = client.preparedQuery("select * from cccat13.account where email = $1")
        .execute(Tuple.of(signupInput.email))
        .await()
      val account = buildAccountFromDatabase(rows)
      if (account != null) throw Exception("Account already exists")
      if (!signupInput.name.matches(Regex("[a-zA-Z]+ [a-zA-Z]+"))) throw Exception("Invalid name")
      if (!signupInput.email.matches(Regex("^(.+)@(.+)\$"))) throw Exception("Invalid email")
      if (!CpfValidator.validate(signupInput.cpf)) throw Exception("Invalid cpf")
      if (signupInput.isDriver && !signupInput.carPlate.matches(Regex("[A-Z]{3}[0-9]{4}"))) throw Exception("Invalid plate")
      client.preparedQuery("insert into cccat13.account (account_id, name, email, cpf, car_plate, is_passenger, is_driver, date, is_verified, verification_code) values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)")
        .execute(
          Tuple.of(
            accountId,
            signupInput.name,
            signupInput.email,
            signupInput.cpf,
            signupInput.carPlate,
            !!signupInput.isPassenger,
            !!signupInput.isDriver,
            date,
            false,
            verificationCode
          )
        )
        .await()
      sendEmail(signupInput.email, "Verification", "Please verify your code at first login $verificationCode")
    } finally {
      client.close()
    }
    return SignupResponse(accountId)
  }

  suspend fun getAccount(accountId: UUID): Account? {
    val client = getSqlClient()
    val rows = client.preparedQuery("select * from cccat13.account where account_id = $1")
      .execute(Tuple.of(accountId))
      .await()
    return buildAccountFromDatabase(rows)
  }

  suspend fun getRide(rideId: UUID): Ride? {
    val client = getSqlClient()
    val rows = client.preparedQuery("select * from cccat13.ride where ride_id = $1")
      .execute(Tuple.of(rideId))
      .await()
    return buildRideFromDatabase(rows)
  }

  private fun getSqlClient(): SqlClient {
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

  suspend fun requestRide(requestRideInput: RequestRideInput): RideRequestedResponse {
    if (requestRideInput.to.isBlank()) {
      throw Exception("Lat/Long should be informed on 'too'")
    }

    if (requestRideInput.from.isBlank()) {
      throw Exception("Lat/Long should be informed on 'from'")
    }

    if (!requestRideInput.to.matches(Regex(VALID_LAT_LONG_REGEX))
      || !requestRideInput.from.matches(Regex(VALID_LAT_LONG_REGEX))
    ) {
      throw Exception("Invalid Lat/Long informed")
    }

    val client = getSqlClient()
    val accountRows = client.preparedQuery("select * from cccat13.account where account_id = $1")
      .execute(Tuple.of(requestRideInput.passengerId))
      .await()
    val account = buildAccountFromDatabase(accountRows) ?: throw Exception("Invalid account informed")

    if (!account.isPassenger) {
      throw Exception("Account informed is not a passenger")
    }

    val rideRows = client.preparedQuery("select * from cccat13.ride where passenger_id = $1 and status != 'COMPLETED'")
      .execute(Tuple.of(requestRideInput.passengerId))
      .await()
    val ride = buildRideFromDatabase(rideRows)
    if (ride != null) {
      throw Exception("Passenger informed have a ride ongoing")
    }

    val (toLat, toLong) = requestRideInput.to.split(", ").map { it.toDouble() }
    val (fromLat, fromLong) = requestRideInput.to.split(", ").map { it.toDouble() }

    val rideId = UUID.randomUUID()
    client.preparedQuery("insert into cccat13.ride (ride_id, passenger_id, status, date, to_lat, to_long, from_lat, from_long) values ($1, $2, $3, $4, $5, $6, $7, $8)")
      .execute(
        Tuple.of(
          rideId,
          requestRideInput.passengerId,
          "REQUESTED",
          LocalDateTime.now(),
          toLat,
          toLong,
          fromLat,
          fromLong
        )
      )
      .await()

    return RideRequestedResponse(rideId)
  }

  private fun buildAccountFromDatabase(rows: RowSet<Row>): Account? {
    val account = if (rows.iterator().hasNext()) {
      val next = rows.iterator().next()
      Account(
        next.getUUID("account_id"),
        next.getString("name"),
        next.getString("email"),
        next.getString("cpf"),
        next.getString("car_plate"),
        next.getBoolean("is_passenger"),
        next.getBoolean("is_driver"),
        next.getLocalDateTime("date"),
        next.getBoolean("is_verified"),
        next.getUUID("verification_code")
      )
    } else null
    return account
  }

  private fun buildRideFromDatabase(rows: RowSet<Row>): Ride? {
    val ride = if (rows.iterator().hasNext()) {
      val next = rows.iterator().next()
      Ride(
        next.getUUID("ride_id"),
        next.getUUID("passenger_id"),
        if (next.getString("driver_id") != null) UUID.fromString(next.getString("driver_id")) else null,
        next.getString("status"),
        next.getDouble("fare"),
        next.getDouble("distance"),
        next.getDouble("from_lat"),
        next.getDouble("from_long"),
        next.getDouble("to_lat"),
        next.getDouble("to_long"),
        next.getLocalDateTime("date")
      )
    } else null
    return ride
  }

  suspend fun acceptRide(acceptRideInput: AcceptRideInput) {
    val account = getAccount(acceptRideInput.driverId) ?: throw Exception("Account not found")
    if (!account.isDriver) throw Exception("Account informed is not a driver")
    val ride = getRide(acceptRideInput.rideId) ?: throw Exception("Ride not found")
    if (ride.status != "REQUESTED") throw Exception("Ride status is not REQUESTED")
    if (!isDriverAvailableToAcceptRide(account.accountId)) throw Exception("Driver is not available")

    updateRideToAccepted(acceptRideInput)
  }

  private suspend fun isDriverAvailableToAcceptRide(driverId: UUID): Boolean {
    val client = getSqlClient()
    val rows = client.preparedQuery("select ride_id from cccat13.ride where driver_id = $1 and status in ('ACCEPTED', 'IN_PROGRESS')")
      .execute(Tuple.of(driverId))
      .await()
    return rows.size() <= 0
  }

  private suspend fun updateRideToAccepted(acceptRideInput: AcceptRideInput) {
    val client = getSqlClient()
    client.preparedQuery("UPDATE cccat13.ride SET driver_id = $1 , status = 'ACCEPTED' WHERE ride_id = $2")
      .execute(
        Tuple.of(acceptRideInput.driverId, acceptRideInput.rideId)
      )
      .await()
  }
}
