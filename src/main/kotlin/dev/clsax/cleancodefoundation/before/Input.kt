package dev.clsax.cleancodefoundation.before

import kotlinx.serialization.Serializable

@Serializable
data class Input(
  var name: String,
  var email: String,
  var cpf: String,
  var isPassenger: Boolean = false,
  var carPlate: String = "",
  var isDriver: Boolean = false
) {

  constructor(
    name: String,
    email: String,
    cpf: String,
    isPassenger: Boolean
  ) : this(name, email, cpf, isPassenger, "", false)

  constructor(
    name: String,
    email: String,
    cpf: String,
    carPlate: String,
    isDriver: Boolean
  ) : this(name, email, cpf, false, carPlate, isDriver)
}
