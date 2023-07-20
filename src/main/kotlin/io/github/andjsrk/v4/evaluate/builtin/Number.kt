package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.ParseException
import kotlin.math.abs

private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

@EsSpec("Number(value)") // (as a normal function)
private val numberFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val value = args[0]
    Completion.Normal(
        when (value) {
            is StringType -> parseNumber(value.value)
            is BigIntType -> value.value.toDouble().languageValue
            is BooleanType -> NumberType(if (value.value) 1.0 else 0.0)
            NullType -> NumberType.POSITIVE_ZERO
            is NumberType -> value
            is ObjectType -> return@fn throwError(TypeErrorKind.OBJECT_TO_NUMBER)
            is SymbolType -> return@fn throwError(TypeErrorKind.SYMBOL_TO_NUMBER)
        }
    )
}

@EsSpec("Number.isFinite")
private val isFinite = BuiltinFunctionType("isFinite", 1u) { _, args ->
    val number = args[0]
    Completion.Normal(
        BooleanType.from(
            number is NumberType && number.isFinite
        )
    )
}

@EsSpec("Number.isInteger")
private val isInteger = BuiltinFunctionType("isInteger", 1u) { _, args ->
    val number = args[0]
    Completion.Normal(
        BooleanType.from(
            number is NumberType && number.isFinite && number.value.isInteger
        )
    )
}

@EsSpec("Number.isNaN")
private val isNaN = BuiltinFunctionType("isNaN", 1u) { _, args ->
    Completion.Normal(
        BooleanType.from(
            args[0] == NumberType.NaN
        )
    )
}

@EsSpec("Number.isSafeInteger")
private val isSafeInteger = BuiltinFunctionType("isSafeInteger", 1u) { _, args ->
    val number = args[0]
    Completion.Normal(
        BooleanType.from(
            number is NumberType
                    && number.value.isInteger
                    && abs(number.value) <= NumberType.MAX_SAFE_INTEGER
        )
    )
}

@EsSpec("Number.parseFloat")
private val parseLeadingDecimal = BuiltinFunctionType("parseLeadingDecimal", 1u) fn@ { _, args ->
    val string = args[0].requireToBe<StringType> { return@fn it }
    // does not perform trim to input string
    val input = string.value
    run {
        val (sign, rest) = getSignAndRest(input)
        if (rest.startsWith("Infinity")) return@fn Completion.Normal(
            if (sign == 1) NumberType.POSITIVE_INFINITY
            else NumberType.NEGATIVE_INFINITY
        )
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
    Completion.Normal(parsed)
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
    Completion.Normal(
        if (validPart.isEmpty()) NumberType.NaN
        else BigInteger(validPart, radix).toDouble().languageValue
    )
}

@EsSpec("Number.prototype.toString") // with radix
private val toRadix = builtinMethod("toRadix", 1u) fn@ { thisArg, args ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    val radix = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRadix { return@fn it }
    if (radix != 10 && number.isFinite && number.not { isInteger }) return@fn throwError(TypeErrorKind.NON_INTEGER_TO_NON_DECIMAL)
    Completion.Normal(
        number.toString(radix)
    )
}

@EsSpec("Number.prototype.toString") // radix is fixed to 10
private val numberToString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    Completion.Normal(
        number.toString(10)
    )
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
        "from".sealedData(numberFrom),
        sealedData(::isFinite),
        sealedData(::isInteger),
        sealedData(::isNaN),
        sealedData(::isSafeInteger),
        sealedData(::parseLeadingDecimal),
        sealedData(::parseLeadingInteger),
        // TODO
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString.sealedData(numberToString),
        sealedData(::toRadix),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Number")
    },
)
