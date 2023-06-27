package io.github.andjsrk.v4

import io.github.andjsrk.v4.parse.foldElvis

internal fun parseNumber(string: String): Double? {
    val parsedNonDecimal = arrayOf("0b" to 2, "0o" to 8, "0x" to 16)
        .asSequence()
        .map { (prefix, radix) ->
            val removed = string.removePrefixOrNull(prefix) ?: return@map null
            removed.toIntOrNull(radix)?.toDouble()
        }
        .foldElvis()
    return parsedNonDecimal ?: string.toDoubleOrNull()
}

private fun String.removePrefixOrNull(prefix: CharSequence) =
    removePrefix(prefix).takeIf { it !== this }
