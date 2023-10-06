package dev.clsax.cleancodefoundation

import dev.clsax.cleancodefoundation.before.AcceptRideInput
import dev.clsax.cleancodefoundation.before.AccountService
import dev.clsax.cleancodefoundation.before.RequestRideInput
import dev.clsax.cleancodefoundation.before.RideService
import dev.clsax.cleancodefoundation.before.SignupInput
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(VertxExtension::class)
class RideServiceTest {

  @Test
  fun `Nao deve solicitar uma corrida com lat long vazio`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val requestRideInputWithoutTo = RequestRideInput(UUID.randomUUID(), "Lorem Ipsum", "")
    val rideService = RideService(vertx)
    val exceptionTo = assertThrows<Exception> { rideService.requestRide(requestRideInputWithoutTo) }
    Assertions.assertEquals("Lat/Long should be informed on 'too'", exceptionTo.message)

    val requestRideInputWithoutFrom = RequestRideInput(UUID.randomUUID(), "", "Lorem Ipsum")
    val exceptionFrom = assertThrows<Exception> { rideService.requestRide(requestRideInputWithoutFrom) }
    Assertions.assertEquals("Lat/Long should be informed on 'from'", exceptionFrom.message)
  }


  @Test
  fun `Nao deve solicitar uma corrida com lat long invalido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val rideService = RideService(vertx)
    val exceptionBothInvalid = assertThrows<Exception> {
      rideService.requestRide(
        RequestRideInput(
          UUID.randomUUID(),
          "Lorem Ipsum",
          "Lorem Ipsum"
        )
      )
    }
    Assertions.assertEquals("Invalid Lat/Long informed", exceptionBothInvalid.message)

    val exceptionToInvalid = assertThrows<Exception> {
      rideService.requestRide(
        RequestRideInput(
          UUID.randomUUID(),
          "45.123, -123.456",
          "Lorem Ipsum"
        )
      )
    }
    Assertions.assertEquals("Invalid Lat/Long informed", exceptionToInvalid.message)

    val exceptionFromInvalid = assertThrows<Exception> {
      rideService.requestRide(
        RequestRideInput(
          UUID.randomUUID(),
          "Lorem Ipsum",
          "-12.3456, 0.789"
        )
      )
    }
    Assertions.assertEquals("Invalid Lat/Long informed", exceptionFromInvalid.message)
  }

  @Test
  fun `Nao deve solicitar corrida para conta invalida`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val rideService = RideService(vertx)
    val requestRideInput =
      RequestRideInput(UUID.randomUUID(), "45.123, -123.456", "-12.3456, 0.789")
    val exception = assertThrows<Exception> { rideService.requestRide(requestRideInput) }
    Assertions.assertEquals("Invalid account informed", exception.message)
  }

  @Test
  fun `Nao deve solicitar corrida para motorista`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput = SignupInput(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AAA9999"
    )
    val rideService = RideService(vertx)
    val accountService = AccountService(vertx)
    val output = accountService.signup(signupInput)
    val requestRideInput =
      RequestRideInput(output.accountId, "45.123, -123.456", "-12.3456, 0.789")
    val exception = assertThrows<Exception> { rideService.requestRide(requestRideInput) }
    Assertions.assertEquals("Account informed is not a passenger", exception.message)
  }

  @Test
  fun `Deve iniciar corrida para passageiro sem corrida em andamento`(vertx: Vertx): Unit =
    runBlocking(vertx.dispatcher()) {
      val signupInput =
        SignupInput(
          name = "John Doe",
          email = "john.doe${Math.random()}@gmail.com",
          cpf = "272.751.500-69",
          isPassenger = true
        )
      val rideService = RideService(vertx)
      val accountService = AccountService(vertx)
      val output = accountService.signup(signupInput)
      val requestRideInput =
        RequestRideInput(output.accountId, "45.123, -123.456", "-12.3456, 0.789")
      rideService.requestRide(requestRideInput)
    }

  @Test
  fun `Nao deve iniciar corrida para passageiro com corrida em andamento`(vertx: Vertx): Unit =
    runBlocking(vertx.dispatcher()) {
      val signupInput =
        SignupInput(
          name = "John Doe",
          email = "john.doe${Math.random()}@gmail.com",
          cpf = "272.751.500-69",
          isPassenger = true
        )
      val rideService = RideService(vertx)
      val accountService = AccountService(vertx)
      val output = accountService.signup(signupInput)
      val requestRideInput =
        RequestRideInput(output.accountId, "45.123, -123.456", "-12.3456, 0.789")
      rideService.requestRide(requestRideInput)
      val exception = assertThrows<Exception> { rideService.requestRide(requestRideInput) }
      Assertions.assertEquals("Passenger informed have a ride ongoing", exception.message)
    }

  @Test
  fun `Deve aceitar uma corrida`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInputPassenger =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isPassenger = true
      )
    val rideService = RideService(vertx)
    val accountService = AccountService(vertx)
    val signupResponse = accountService.signup(signupInputPassenger)
    val requestRideInput =
      RequestRideInput(signupResponse.accountId, "45.123, -123.456", "-12.3456, 0.789")
    val requestRideResponse = rideService.requestRide(requestRideInput)

    val signupInputDriver = SignupInput(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AAA9999"
    )
    val driverSignupResponse = accountService.signup(signupInputDriver)

    val acceptRideInput = AcceptRideInput(requestRideResponse.rideId, driverSignupResponse.accountId)
    rideService.acceptRide(acceptRideInput)
  }

  @Test
  fun `Nao deve aceitar uma corrida quando o driver informado nao e driver`(vertx: Vertx): Unit =
    runBlocking(vertx.dispatcher()) {
      val signupInputPassenger =
        SignupInput(
          name = "John Doe",
          email = "john.doe${Math.random()}@gmail.com",
          cpf = "272.751.500-69",
          isPassenger = true
        )
      val rideService = RideService(vertx)
      val accountService = AccountService(vertx)
      val signupResponse = accountService.signup(signupInputPassenger)
      val requestRideInput =
        RequestRideInput(signupResponse.accountId, "45.123, -123.456", "-12.3456, 0.789")
      val requestRideResponse = rideService.requestRide(requestRideInput)

      val signupInputDriver = SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isDriver = false,
        carPlate = "AAA9999"
      )
      val driverSignupResponse = accountService.signup(signupInputDriver)

      val acceptRideInput = AcceptRideInput(requestRideResponse.rideId, driverSignupResponse.accountId)
      val exception = assertThrows<Exception> { rideService.acceptRide(acceptRideInput) }
      Assertions.assertEquals("Account informed is not a driver", exception.message)
    }

  @Test
  fun `Nao deve aceitar uma corrida quando status for diferente de requested`(vertx: Vertx): Unit =
    runBlocking(vertx.dispatcher()) {
      val signupInputPassenger =
        SignupInput(
          name = "John Doe",
          email = "john.doe${Math.random()}@gmail.com",
          cpf = "272.751.500-69",
          isPassenger = true
        )
      val rideService = RideService(vertx)
      val accountService = AccountService(vertx)
      val signupResponse = accountService.signup(signupInputPassenger)
      val requestRideInput =
        RequestRideInput(signupResponse.accountId, "45.123, -123.456", "-12.3456, 0.789")
      val (rideId) = rideService.requestRide(requestRideInput)

      val signupInputDriver = SignupInput(
        name = "John Driver",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isDriver = true,
        carPlate = "AAA9999"
      )
      val (accountId) = accountService.signup(signupInputDriver)

      val acceptRideInput = AcceptRideInput(rideId, accountId)
      rideService.acceptRide(acceptRideInput)
    }
}
