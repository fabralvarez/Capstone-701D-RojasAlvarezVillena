package com.example.vitalarmapp.utils.validation

/**
 * Implementación local de la validación de RUT chileno utilizando el algoritmo módulo 11.
 */
class LocalRutValidationService : RutValidationService {

    override suspend fun isValid(rut: String): Boolean {
        val cleanedRut = rut.uppercase().filter { it.isLetterOrDigit() }
        if (cleanedRut.length < MIN_RUT_LENGTH) return false

        val body = cleanedRut.dropLast(1)
        val verifier = cleanedRut.last()

        if (body.any { !it.isDigit() }) return false

        val expectedVerifier = calculateVerifier(body)
        return expectedVerifier == verifier
    }

    private fun calculateVerifier(body: String): Char {
        var multiplier = MULTIPLIER_START
        var sum = 0

        for (digitChar in body.reversed()) {
            val digit = digitChar.digitToInt()
            sum += digit * multiplier
            multiplier = if (multiplier == MULTIPLIER_MAX) MULTIPLIER_START else multiplier + 1
        }

        val remainder = 11 - (sum % 11)
        return when (remainder) {
            11 -> '0'
            10 -> 'K'
            else -> remainder.digitToChar()
        }
    }

    companion object {
        private const val MIN_RUT_LENGTH = 2
        private const val MULTIPLIER_START = 2
        private const val MULTIPLIER_MAX = 7
    }
}
