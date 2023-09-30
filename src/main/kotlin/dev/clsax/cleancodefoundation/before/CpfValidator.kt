package dev.clsax.cleancodefoundation.before

class CpfValidator {
  companion object {
    fun validate(cpf: String): Boolean {
      if (cpf.isEmpty()) return false
      var cleanedCpf = clean(cpf)
      if (isInvalidLenght(cleanedCpf)) return false
      if (allDigitsTheSame(cleanedCpf)) return false
      var firstDigit = calculateDigit(cleanedCpf, 10)
      var secondDigit = calculateDigit(cleanedCpf, 11)
      return extractDigit(cleanedCpf) == "$firstDigit$secondDigit"
    }

    private fun clean(cpf: String) = cpf.replace(Regex("[^\\d]"), "")

    private fun isInvalidLenght(cleanedCpf: String) = cleanedCpf.length != 11

    private fun allDigitsTheSame(cleanedCpf: String) = cleanedCpf.all { it == cleanedCpf[0] }

    private fun calculateDigit(cpf: String, factor: Int): Int {
      var total = 0
      var factor = factor
      cpf.forEach {
        if (factor > 1) total += it.digitToInt() * factor--
      }
      val rest = total % 11
      return if (rest < 2) 0 else 11 - rest
    }

    private fun extractDigit(cleanedCpf: String) = cleanedCpf.substring(cleanedCpf.length - 2, cleanedCpf.length)
  }
}
