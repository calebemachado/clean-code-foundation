package dev.clsax.cleancodefoundation

import java.util.*

interface RideDAO {

  suspend fun getRideByPassengerAndStatusNotCompleted(passengerId: UUID): Ride?
  suspend fun save(passengerId: UUID, toLat: Double, toLong: Double, fromLat: Double, fromLong: Double): UUID
  suspend fun getRideById(rideId: UUID): Ride?
  suspend fun isDriverAvailable(driverId: UUID): Boolean
  suspend fun acceptRide(rideId: UUID, driverId: UUID)
}
