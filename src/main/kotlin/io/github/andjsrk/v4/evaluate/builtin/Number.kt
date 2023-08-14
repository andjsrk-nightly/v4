package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.toNormal
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.ParseException
import kotlin.math.abs

private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

@EsSpec("Number(value)")
private val numberFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val value = args[0]
    when (value) {
        is StringType -> parseNumber(value.value)
        is BigIntType -> value.value.toDouble().languageValue
        is BooleanType -> NumberType(if (value.value) 1.0 else 0.0)
        NullType -> NumberType.POSITIVE_ZERO
        is NumberType -> value
        is ObjectType -> return@fn throwError(TypeErrorKind.OBJECT_TO_NUMBER)
        is SymbolType -> return@fn throwError(TypeErrorKind.SYMBOL_TO_NUMBER)
    }
        .toNormal()
}

@EsSpec("Number.isFinite")
private val isFinite = BuiltinFunctionType("isFinite", 1u) { _, args ->
    val number = args[0]
    (number is NumberType && number.isFinite)
        .languageValue
        .toNormal()
}

@EsSpec("Number.isInteger")
private val isInteger = BuiltinFunctionType("isInteger", 1u) { _, args ->
    val number = args[0]
    (number is NumberType && number.isFinite && number.value.isInteger)
        .languageValue
        .toNormal()
}

@EsSpec("Number.isNaN")
private val isNaN = BuiltinFunctionType("isNaN", 1u) { _, args ->
    (args[0] == NumberType.NaN)
        .languageValue
        .toNormal()
}

@EsSpec("Number.isSafeInteger")
private val isSafeInteger = BuiltinFunctionType("isSafeInteger", 1u) { _, args ->
    val number = args[0]
    (
        number is NumberType
            && number.value.isInteger
            && abs(number.value) <= NumberType.MAX_SAFE_INTEGER
    )
        .languageValue
        .toNormal()
}

@EsSpec("Number.parseFloat")
private val parseLeadingDecimal = BuiltinFunctionType("parseLeadingDecimal", 1u) fn@ { _, args ->
    val string = args[0].requireToBe<StringType> { return@fn it }
    // does not perform trim to input string
    val input = string.value
    run {
        val (sign, rest) = getSignAndRest(input)
        if (rest.startsWith("Infinity")) return@fn (
            if (sign == 1) NumberType.POSITIVE_INFINITY
            else NumberType.NEGATIVE_INFINITY
        )
            .toNormal()
    }
    val uppercaseInput = input
        .uppercase() // DecimalFormat does not recognize scientific notation with lowercase 'e', so making it uppercase
    val parsed =
        try {
            DecimalFormat.getInstance().parse(uppercaseInput)
                .toDouble()
                .languageValue
        } catch (e: ParseException) {
            NumberType.NaN
        }
    parsed.toNormal()
}

@EsSpec("Number.parseInt")
private val parseLeadingInteger = BuiltinFunctionType("parseLeadingInteger", 1u) fn@ { _, args ->
    val string = args[0].requireToBe<StringType> { return@fn it }
    val radix = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    val digitCharsForRadix = DIGITS.substring(0, radix)
    val validPart = string.value.takeWhile { digitCharsForRadix.contains(it, ignoreCase=true) }
    (
        if (validPart.isEmpty()) NumberType.NaN
        else BigInteger(validPart, radix).toDouble().languageValue
    )
        .toNormal()
}

@EsSpec("Number.prototype.toString")
private val numberToRadix = builtinMethod("toRadix", 1u) fn@ { thisArg, args ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    val radix = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRadix { return@fn it }
    if (radix != 10 && number.isFinite && number.not { isInteger }) return@fn throwError(TypeErrorKind.NON_INTEGER_TO_NON_DECIMAL)
    number.toString(radix)
        .toNormal()
}

@EsSpec("Number.prototype.toString") // radix is fixed to 10
private val numberToString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    number.toString(10)
        .toNormal()
}

@EsSpec("%Number%")
val Number = BuiltinClassType(
    "Number",
    Object,
    mutableMapOf(
        "EPSILON".sealedData(NumberType.EPSILON.languageValue),
        "MAX_SAFE_INTEGER".sealedData(NumberType.MAX_SAFE_INTEGER.languageValue),
        "MAX_VALUE".sealedData(NumberType.MAX_VALUE.languageValue),
        "MIN_SAFE_INTEGER".sealedData(NumberType.MIN_SAFE_INTEGER.languageValue),
        "MIN_VALUE".sealedData(NumberType.MIN_VALUE.languageValue),
        "NaN".sealedData(NumberType.NaN),
        "NEGATIVE_INFINITY".sealedData(NumberType.NEGATIVE_INFINITY),
        "POSITIVE_INFINITY".sealedData(NumberType.POSITIVE_INFINITY),
        sealedMethod(numberFrom),
        sealedMethod(isFinite),
        sealedMethod(isInteger),
        sealedMethod(isNaN),
        sealedMethod(isSafeInteger),
        sealedMethod(parseLeadingDecimal),
        sealedMethod(parseLeadingInteger),
        // TODO
    ),
    mutableMapOf(
        sealedMethod(numberToString),
        sealedMethod(numberToRadix),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Number")
    },
)
