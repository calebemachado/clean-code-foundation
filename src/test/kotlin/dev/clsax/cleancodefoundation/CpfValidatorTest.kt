package dev.clsax.cleancodefoundation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CpfValidatorTest {

  @ParameterizedTest(name = "Deve retornar true quando cpf = {0}")
  @ValueSource(strings = ["769.782.180-06", "95818705552", "565.486.780-60", "147.864.110-00"])
  fun `Deve retornar true quando cpf for válido`(input: String) {
    Assertions.assertTrue(CpfValidator.validate(input))
  }

  @ParameterizedTest(name = "Deve retornar false quando cpf = {0}")
  @ValueSource(strings = ["11111111111", "1234567890", "...---...---.-", "123456789012345"])
  fun `Deve retornar false quando cpf tiver todos numeros iguais`(input: String) {
    Assertions.assertFalse(CpfValidator.validate(input))
  }
}
