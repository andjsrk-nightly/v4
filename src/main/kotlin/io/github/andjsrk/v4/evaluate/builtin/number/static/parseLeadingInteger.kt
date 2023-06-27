package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.isInteger
import io.github.andjsrk.v4.not
import java.math.BigInteger

private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

val parseLeadingInteger = BuiltinFunctionType("parseLeadingInteger", 1u) fn@ { _, args ->
    val string = args[0]
    string.requireToBe<StringType> { return@fn it }
    val radixArg = args.getOrNull(1).normalizeNull()
    radixArg.requireToBe<NumberType?> { return@fn it }
    if (
        radixArg != null && (
            radixArg.not { isFinite }
                || radixArg.value.not { isInteger }
                || radixArg.value !in 2.0..36.0
        )
    ) return@fn Completion.Throw(NullType/* RangeError */)
    val radix = radixArg?.value?.toInt() ?: 10
    val digitCharsForRadix = DIGITS.substring(0, radix)
    val validPart = string.value.takeWhile { digitCharsForRadix.contains(it, ignoreCase=true) }
    Completion.Normal(
        if (validPart.isEmpty()) NumberType.NaN
        else BigInteger(validPart, radix).toDouble().languageValue
    )
}
