package io.github.andjsrk.v4

import io.github.andjsrk.v4.evaluate.getSignAndRest
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.NumberType
import io.github.andjsrk.v4.parse.foldElvis
import kotlin.math.withSign

@EsSpec("StringToNumber")
internal fun parseNumber(string: String): NumberType {
    val (sign, rest) = getSignAndRest(string)
    if (rest == "Infinity") return (
        if (sign == 1) NumberType.POSITIVE_INFINITY
        else NumberType.NEGATIVE_INFINITY
    )
    val parsedNonDecimal = arrayOf("0b" to 2, "0o" to 8, "0x" to 16)
        .asSequence()
        .map { (prefix, radix) ->
            val removed = rest.removePrefixOrNull(prefix) ?: return@map null
            removed.toDoubleOrNull(radix)
        }
        .foldElvis()
    val parsed = parsedNonDecimal
        ?: (rest.getOrNull(0)?.isDecimalDigit ?: false).thenTake {
            rest.toDoubleOrNull()
        }
    return parsed?.withSign(sign)?.languageValue ?: NumberType.NaN
}

/**
 * @see toUIntOrNull
 */
private fun String.toDoubleOrNull(radix: Int): Double? {
    checkRadix(radix)

    val length = length
    if (length == 0) return null

    val limit = Double.MAX_VALUE
    val limitForMaxRadix = limit / 36

    var limitBeforeMul = limitForMaxRadix
    var result = 0.0
    for (i in 0 until length) {
        val digit = Character.digit(this[i], radix)

        if (digit < 0) return null
        if (result > limitBeforeMul) {
            if (limitBeforeMul == limitForMaxRadix) {
                limitBeforeMul = limit / radix

                if (result > limitBeforeMul) return null
            } else return null
        }

        result *= radix
        result += digit
    }

    return result
}

/**
 * @see kotlin.text.checkRadix
 */
private fun checkRadix(radix: Int): Int {
    if (radix !in Character.MIN_RADIX..Character.MAX_RADIX) {
        throw IllegalArgumentException("radix $radix was not in valid range ${Character.MIN_RADIX..Character.MAX_RADIX}")
    }
    return radix
}
private fun String.removePrefixOrNull(prefix: CharSequence) =
    removePrefix(prefix).takeIf { it !== this }
