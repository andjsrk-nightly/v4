package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import java.math.BigInteger

private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

@EsSpec("Number.parseInt")
val parseLeadingInteger = BuiltinFunctionType("parseLeadingInteger", 1u) fn@ { _, args ->
    val string = args[0].requireToBe<StringType> { return@fn it }
    val radix = args.getOrNull(1)
        ?.normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    val digitCharsForRadix = DIGITS.substring(0, radix)
    val validPart = string.value.takeWhile { digitCharsForRadix.contains(it, ignoreCase=true) }
    Completion.Normal(
        if (validPart.isEmpty()) NumberType.NaN
        else BigInteger(validPart, radix).toDouble().languageValue
    )
}
