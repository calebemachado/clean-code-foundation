package dev.clsax.cleancodefoundation

import java.time.LocalDateTime
import java.util.*

data class Account(
  val accountId: UUID,
  val name: String,
  val email: String,
  val cpf: String,
  val carPlate: String,
  val isPassenger: Boolean,
  val isDriver: Boolean,
  val date: LocalDateTime,
  val isVerified: Boolean,
  val verificationCode: UUID?
)
