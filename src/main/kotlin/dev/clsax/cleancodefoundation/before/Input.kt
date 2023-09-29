package dev.clsax.cleancodefoundation.before

data class Input(
  var name: String,
  var email: String,
  var cpf: String,
  var isPassenger: Boolean,
  var carPlate: String,
  var isDriver: Boolean
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
