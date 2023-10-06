package dev.clsax.cleancodefoundation.before

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import java.util.*

class DatabaseMapperUtils {
  companion object {
    fun buildAccountFromDatabase(rows: RowSet<Row>): Account? {
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

    fun buildRideFromDatabase(rows: RowSet<Row>): Ride? {
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
  }
}
