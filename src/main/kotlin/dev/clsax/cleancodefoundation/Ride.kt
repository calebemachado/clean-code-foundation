package dev.clsax.cleancodefoundation

import java.time.LocalDateTime
import java.util.*

data class Ride(
  val rideId: UUID,
  val passengerId: UUID,
  val driverId: UUID?,
  val status: String,
  val fare: Double?,
  val distance: Double?,
  val fromLat: Double,
  val fromLong: Double,
  val toLat: Double,
  val toLong: Double,
  val date: LocalDateTime
)
