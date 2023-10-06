package dev.clsax.cleancodefoundation

import io.vertx.core.Vertx

class RideService(vertx: Vertx) {

  private val rideDAO: RideDAO
  private val accountDAO: AccountDAO

  init {
    rideDAO = RideDAODatabase(vertx)
    accountDAO = AccountDAODatabase(vertx)
  }

  companion object {
    private const val VALID_LAT_LONG_REGEX =
      "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$"
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

    val account = accountDAO.getAccountById(requestRideInput.passengerId)
      ?: throw Exception("Invalid account informed")

    if (!account.isPassenger) {
      throw Exception("Account informed is not a passenger")
    }

    val ride = rideDAO.getRideByPassengerAndStatusNotCompleted(requestRideInput.passengerId)
    if (ride != null) {
      throw Exception("Passenger informed have a ride ongoing")
    }

    val (toLat, toLong) = requestRideInput.to.split(", ").map { it.toDouble() }
    val (fromLat, fromLong) = requestRideInput.to.split(", ").map { it.toDouble() }

    return RideRequestedResponse(rideDAO.save(requestRideInput.passengerId, toLat, toLong, fromLat, fromLong))
  }

  suspend fun acceptRide(acceptRideInput: AcceptRideInput) {
    val account = accountDAO.getAccountById(acceptRideInput.driverId) ?: throw Exception("Account not found")
    if (!account.isDriver) throw Exception("Account informed is not a driver")
    val ride = rideDAO.getRideById(acceptRideInput.rideId) ?: throw Exception("Ride not found")
    if (ride.status != "REQUESTED") throw Exception("Ride status is not REQUESTED")
    if (!rideDAO.isDriverAvailable(account.accountId)) throw Exception("Driver is not available")

    rideDAO.acceptRide(acceptRideInput.rideId, acceptRideInput.driverId)
  }
}
