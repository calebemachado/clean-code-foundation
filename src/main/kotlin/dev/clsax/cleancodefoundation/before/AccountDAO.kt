package dev.clsax.cleancodefoundation.before

import java.util.*

interface AccountDAO {
  suspend fun save(
    name: String,
    email: String,
    cpf: String,
    carPlate: String,
    isPassenger: Boolean,
    isDriver: Boolean,
    verificationCode: UUID
  ): UUID

  suspend fun getAccountById(accountId: UUID): Account?
  suspend fun getAccountByEmail(accountEmail: String): Account?
}
