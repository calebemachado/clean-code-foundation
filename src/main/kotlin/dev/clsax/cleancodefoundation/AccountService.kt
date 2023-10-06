package dev.clsax.cleancodefoundation

import io.vertx.core.Vertx
import java.util.*

class AccountService(vertx: Vertx) {

  private val accountDAO: AccountDAO

  init {
    accountDAO = AccountDAODatabase(vertx)
  }

  suspend fun signup(signupInput: SignupInput): SignupResponse {
    val verificationCode = UUID.randomUUID()
    val account = accountDAO.getAccountByEmail(signupInput.email)
    if (account != null) throw Exception("Account already exists")
    if (!signupInput.name.matches(Regex("[a-zA-Z]+ [a-zA-Z]+"))) throw Exception("Invalid name")
    if (!signupInput.email.matches(Regex("^(.+)@(.+)\$"))) throw Exception("Invalid email")
    if (!CpfValidator.validate(signupInput.cpf)) throw Exception("Invalid cpf")
    if (signupInput.isDriver && !signupInput.carPlate.matches(Regex("[A-Z]{3}[0-9]{4}"))) throw Exception("Invalid plate")
    val accountId = accountDAO.save(
      signupInput.name,
      signupInput.email,
      signupInput.cpf,
      signupInput.carPlate,
      signupInput.isPassenger,
      signupInput.isDriver,
      verificationCode
    )
    sendEmail(signupInput.email, "Verification", "Please verify your code at first login $verificationCode")

    return SignupResponse(accountId)
  }

  suspend fun getAccount(accountId: UUID): Account? {
    return accountDAO.getAccountById(accountId)
  }

  private fun sendEmail(email: String, subject: String, message: String) {
    println("$email $subject $message")
  }
}
