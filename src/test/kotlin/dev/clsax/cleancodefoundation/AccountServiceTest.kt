package dev.clsax.cleancodefoundation

import dev.clsax.cleancodefoundation.before.AccountService
import dev.clsax.cleancodefoundation.before.Input
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
    val input =
      Input(name = "John Doe", email = "john.doe${Math.random()}@gmail.com", cpf = "272.751.500-69", isPassenger = true)
    //when
    val accountService = AccountService(vertx)
    val output = accountService.signup(input)
    //then
    val account = accountService.getAccount(output.accountId)
    Assertions.assertNotNull(account)
    Assertions.assertNotNull(account?.accountId)
    Assertions.assertEquals(input.name, account?.name)
  }

  @Test
  fun `Deve retornar null quando buscar conta com id que não existe`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
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
    val input =
      Input(name = "John", email = "john.doe${Math.random()}@gmail.com", cpf = "272.751.500-00", isPassenger = true)
    //when
    val accountService = AccountService(vertx)
    //then
    val exception = assertThrows<Exception> { accountService.signup(input) }
    Assertions.assertEquals("Invalid name", exception.message)
  }

  @Test
  fun `Não deve criar um passageiro com email inválido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val input = Input(name = "John Doe", email = "gmail.com", cpf = "272.751.500-00", isPassenger = true)
    //when
    val accountService = AccountService(vertx)
    //then
    val exception = assertThrows<Exception> { accountService.signup(input) }
    Assertions.assertEquals("Invalid email", exception.message)
  }

  @Test
  fun `Não deve criar um passageiro com CPF inválido`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val input =
      Input(name = "John Doe", email = "john.doe${Math.random()}@gmail.com", cpf = "272.751.500-00", isPassenger = true)
    //when
    val accountService = AccountService(vertx)

    //then
    val exception = assertThrows<Exception> { accountService.signup(input) }
    Assertions.assertEquals("Invalid cpf", exception.message)
  }

  @Test
  fun `Deve criar um motorista`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val input = Input(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AAA9999"
    )
    //when
    val accountService = AccountService(vertx)
    val output = accountService.signup(input)
    //then
    Assertions.assertNotNull(output.accountId)
  }

  @Test
  fun `Não deve criar um motorista com placa inválida`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val input = Input(
      name = "John Doe",
      email = "john.doe${Math.random()}@gmail.com",
      cpf = "272.751.500-69",
      isDriver = true,
      carPlate = "AA9999"
    )
    //when
    val accountService = AccountService(vertx)
    //then
    val exception = assertThrows<Exception> { accountService.signup(input) }
    Assertions.assertEquals("Invalid plate", exception.message)
  }

  @Test
  fun `Não deve criar uma conta com mesmo email`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    //given
    val input =
      Input(name = "John Doe", email = "john.doe${Math.random()}@gmail.com", cpf = "272.751.500-69", isPassenger = true)
    //when
    val accountService = AccountService(vertx)
    //then
    accountService.signup(input)
    val exception = assertThrows<Exception> { accountService.signup(input) }
    Assertions.assertEquals("Account already exists", exception.message)
  }

  @Test
  fun `Deve retornar false quando cpf maior que 14`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val accountService = AccountService(vertx)
    val validateCpf = accountService.validateCpf("123456789012345")
    Assertions.assertFalse(validateCpf)
  }

  @Test
  fun `Deve retornar false quando cpf menor que 11`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val accountService = AccountService(vertx)
    val validateCpf = accountService.validateCpf("1234567890")
    Assertions.assertFalse(validateCpf)
  }

  @Test
  fun `Deve retornar false quando cpf conter apenas digitos`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val accountService = AccountService(vertx)
    val validateCpf = accountService.validateCpf("...---...---.-")
    Assertions.assertFalse(validateCpf)
  }
}
