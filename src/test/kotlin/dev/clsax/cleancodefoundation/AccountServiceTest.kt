package dev.clsax.cleancodefoundation

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
    val signupInput =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isPassenger = true
      )
    val accountService = AccountService(vertx)
    val output = accountService.signup(signupInput)
    val account = accountService.getAccount(output.accountId)
    Assertions.assertNotNull(account)
    Assertions.assertNotNull(account?.accountId)
    Assertions.assertEquals(signupInput.name, account?.name)
  }

  @Test
  fun `Deve retornar null quando buscar conta com id que nao existe`(vertx: Vertx): Unit =
    runBlocking(vertx.dispatcher()) {
      val randomUUID = UUID.randomUUID()
      val accountService = AccountService(vertx)
      val account = accountService.getAccount(randomUUID)
      Assertions.assertNull(account)
    }

  @Test
  fun `Nao deve criar um passageiro com nome invalido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput =
      SignupInput(
        name = "John",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-00",
        isPassenger = true
      )
    val accountService = AccountService(vertx)
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid name", exception.message)
  }

  @Test
  fun `Nao deve criar um passageiro com email invalido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput = SignupInput(name = "John Doe", email = "gmail.com", cpf = "272.751.500-00", isPassenger = true)
    val accountService = AccountService(vertx)
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid email", exception.message)
  }

  @Test
  fun `Nao deve criar um passageiro com CPF invalido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-00",
        isPassenger = true
      )
    val accountService = AccountService(vertx)

    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid cpf", exception.message)
  }

  @Test
  fun `Deve criar um motorista`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput = SignupInput(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AAA9999"
    )
    val accountService = AccountService(vertx)
    val output = accountService.signup(signupInput)
    Assertions.assertNotNull(output.accountId)
  }

  @Test
  fun `Nao deve criar um motorista com placa invalida`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput = SignupInput(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AA9999"
    )
    val accountService = AccountService(vertx)
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Invalid plate", exception.message)
  }

  @Test
  fun `Nao deve criar uma conta com mesmo email`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val signupInput =
      SignupInput(
        name = "John Doe",
        email = "john.doe${Math.random()}@gmail.com",
        cpf = "272.751.500-69",
        isPassenger = true
      )
    val accountService = AccountService(vertx)
    accountService.signup(signupInput)
    val exception = assertThrows<Exception> { accountService.signup(signupInput) }
    Assertions.assertEquals("Account already exists", exception.message)
  }

}
