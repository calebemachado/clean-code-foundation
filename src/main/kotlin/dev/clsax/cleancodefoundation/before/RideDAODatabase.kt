package dev.clsax.cleancodefoundation.before

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.time.LocalDateTime
import java.util.*


class RideDAODatabase(private val vertx: Vertx) : RideDAO {

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

  override suspend fun getRideByPassengerAndStatusNotCompleted(passengerId: UUID): Ride? {
    val client = getSqlClient(vertx)
    val rideRows = client.preparedQuery("select * from cccat13.ride where passenger_id = $1 and status != 'COMPLETED'")
      .execute(Tuple.of(passengerId))
      .await()
    client.close()
    return DatabaseMapperUtils.buildRideFromDatabase(rideRows)
  }

  override suspend fun save(
    passengerId: UUID,
    toLat: Double,
    toLong: Double,
    fromLat: Double,
    fromLong: Double
  ): UUID {
    val client = getSqlClient(vertx)
    val rideId = UUID.randomUUID()
    client.preparedQuery("insert into cccat13.ride (ride_id, passenger_id, status, date, to_lat, to_long, from_lat, from_long) values ($1, $2, $3, $4, $5, $6, $7, $8)")
      .execute(
        Tuple.of(
          rideId,
          passengerId,
          "REQUESTED",
          LocalDateTime.now(),
          toLat,
          toLong,
          fromLat,
          fromLong
        )
      )
      .await()
    client.close()
    return rideId
  }

  override suspend fun getRideById(rideId: UUID): Ride? {
    val client = getSqlClient(vertx)
    val rows = client.preparedQuery("select * from cccat13.ride where ride_id = $1")
      .execute(Tuple.of(rideId))
      .await()
    client.close()
    return DatabaseMapperUtils.buildRideFromDatabase(rows)
  }

  override suspend fun isDriverAvailable(driverId: UUID): Boolean {
    val client = getSqlClient(vertx)
    val rows =
      client.preparedQuery("select ride_id from cccat13.ride where driver_id = $1 and status in ('ACCEPTED', 'IN_PROGRESS')")
        .execute(Tuple.of(driverId))
        .await()
    client.close()
    return rows.size() <= 0
  }

  override suspend fun acceptRide(rideId: UUID, driverId: UUID) {
    val client = getSqlClient(vertx)
    client.preparedQuery("UPDATE cccat13.ride SET driver_id = $1 , status = 'ACCEPTED' WHERE ride_id = $2")
      .execute(
        Tuple.of(driverId, rideId)
      )
      .await()
    client.close()
  }
}
