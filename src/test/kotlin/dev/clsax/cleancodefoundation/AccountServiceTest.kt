package dev.clsax.cleancodefoundation

import dev.clsax.cleancodefoundation.before.AccountService
import dev.clsax.cleancodefoundation.before.RequestRideInput
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
class AccountServiceTest {

  @Test
  fun `Deve criar um passageiro`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isPassenger = true
      )
    //when
    val accountService = AccountService(vertx)
    val output = accountService.signup(signupInput)
    //then
    val account = accountService.getAccount(output.accountId)
    Assertions.assertNotNull(account)
    Assertions.assertNotNull(account?.accountId)
    Assertions.assertEquals(signupInput.name, account?.name)
  }

  @Test
  fun `Deve retornar null quando buscar conta com id que não existe`(vertx: Vertx): Unit =
    runBlocking(vertx.dispatcher()) {
      //given
      val randomUUID = UUID.randomUUID()
      //when
      val accountService = AccountService(vertx)
      val account = accountService.getAccount(randomUUID)
      //then
      Assertions.assertNull(account)
    }

  @Test
  fun `Não deve criar um passageiro com nome inválido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput =
      SignupInput(
        name = "John",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-00",
        isPassenger = true
      )
    //when
    val accountService = AccountService(vertx)
    //then
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid name", exception.message)
  }

  @Test
  fun `Não deve criar um passageiro com email inválido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput = SignupInput(name = "John Doe", email = "gmail.com", cpf = "272.751.500-00", isPassenger = true)
    //when
    val accountService = AccountService(vertx)
    //then
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid email", exception.message)
  }

  @Test
  fun `Não deve criar um passageiro com CPF inválido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-00",
        isPassenger = true
      )
    //when
    val accountService = AccountService(vertx)

    //then
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid cpf", exception.message)
  }

  @Test
  fun `Deve criar um motorista`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput = SignupInput(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AAA9999"
    )
    //when
    val accountService = AccountService(vertx)
    val output = accountService.signup(signupInput)
    //then
    Assertions.assertNotNull(output.accountId)
  }

  @Test
  fun `Não deve criar um motorista com placa inválida`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput = SignupInput(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AA9999"
    )
    //when
    val accountService = AccountService(vertx)
    //then
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid plate", exception.message)
  }

  @Test
  fun `Não deve criar uma conta com mesmo email`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val signupInput =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isPassenger = true
      )
    //when
    val accountService = AccountService(vertx)
    //then
    accountService.signup(signupInput)
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Account already exists", exception.message)
  }

  @Test
  fun `Nao deve solicitar uma corrida com lat long vazio`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val requestRideInputWithoutTo = RequestRideInput(UUID.randomUUID(), "Lorem Ipsum", "")
    val accountService = AccountService(vertx)
    val exceptionTo = assertThrows<Exception> { accountService.requestRide(requestRideInputWithoutTo) }
    Assertions.assertEquals("Lat/Long should be informed on 'too'", exceptionTo.message)

    val requestRideInputWithoutFrom = RequestRideInput(UUID.randomUUID(), "", "Lorem Ipsum")
    val exceptionFrom = assertThrows<Exception> { accountService.requestRide(requestRideInputWithoutFrom) }
    Assertions.assertEquals("Lat/Long should be informed on 'from'", exceptionFrom.message)
  }


  @Test
  fun `Nao deve solicitar uma corrida com lat long invalido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val accountService = AccountService(vertx)
    val exceptionBothInvalid = assertThrows<Exception> {
      accountService.requestRide(
        RequestRideInput(
          UUID.randomUUID(),
          "Lorem Ipsum",
          "Lorem Ipsum"
        )
      )
    }
    Assertions.assertEquals("Invalid Lat/Long informed", exceptionBothInvalid.message)

    val exceptionToInvalid = assertThrows<Exception> {
      accountService.requestRide(
        RequestRideInput(
          UUID.randomUUID(),
          "45.123, -123.456",
          "Lorem Ipsum"
        )
      )
    }
    Assertions.assertEquals("Invalid Lat/Long informed", exceptionToInvalid.message)

    val exceptionFromInvalid = assertThrows<Exception> {
      accountService.requestRide(
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
    val accountService = AccountService(vertx)
    val requestRideInput =
      RequestRideInput(UUID.randomUUID(), "45.123, -123.456", "-12.3456, 0.789")
    val exception = assertThrows<Exception> { accountService.requestRide(requestRideInput) }
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
    val accountService = AccountService(vertx)
    val output = accountService.signup(signupInput)
    val requestRideInput =
      RequestRideInput(output.accountId, "45.123, -123.456", "-12.3456, 0.789")
    val exception = assertThrows<Exception> { accountService.requestRide(requestRideInput) }
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
      val accountService = AccountService(vertx)
      val output = accountService.signup(signupInput)
      val requestRideInput =
        RequestRideInput(output.accountId, "45.123, -123.456", "-12.3456, 0.789")
      accountService.requestRide(requestRideInput)
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
      val accountService = AccountService(vertx)
      val output = accountService.signup(signupInput)
      val requestRideInput =
        RequestRideInput(output.accountId, "45.123, -123.456", "-12.3456, 0.789")
      accountService.requestRide(requestRideInput)
      val exception = assertThrows<Exception> { accountService.requestRide(requestRideInput) }
      Assertions.assertEquals("Passenger informed have a ride ongoing", exception.message)
    }

}
