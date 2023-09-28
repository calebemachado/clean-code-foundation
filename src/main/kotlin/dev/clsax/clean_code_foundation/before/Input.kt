package dev.clsax.clean_code_foundation.before

data class Input(
  var email: String,
  var cpf: String,
  var carPlate: String,
  var name: String,
  var isDriver: Boolean,
  var isPassenger: Boolean
)
