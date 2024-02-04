package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.not
import kotlin.math.*
import kotlin.random.Random
import kotlin.math.log10 as kotlinLog10
import kotlin.math.log2 as kotlinLog2

@EsSpec("Math.abs")
val abs = functionWithoutThis("abs", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    (if (x.isNegative) -x else x)
        .toNormal()
}

@EsSpec("Math.acos")
val acos = functionWithoutThis("acos", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.value > 1.0 || x.value < 1.0) return@fn NumberType.NaN.toNormal()
    acos(x.value)
        .toNormal()
}

@EsSpec("Math.acosh")
val acosh = functionWithoutThis("acosh", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isPositiveInfinity) return@fn x.toNormal()
    if (x.value == 1.0) return@fn NumberType.POSITIVE_ZERO.toNormal()
    if (x.value < 1.0) return@fn NumberType.NaN.toNormal()
    acosh(x.value)
        .toNormal()
}

@EsSpec("Math.asin")
val asin = functionWithoutThis("asin", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero) return@fn x.toNormal()
    if (x.value > 1.0 || x.value < -1.0) return@fn NumberType.NaN.toNormal()
    asin(x.value)
        .toNormal()
}

@EsSpec("Math.asinh")
val asinh = functionWithoutThis("asinh", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isZero) return@fn x.toNormal()
    asinh(x.value)
        .toNormal()
}

@EsSpec("Math.atan")
val atan = functionWithoutThis("atan", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero) return@fn x.toNormal()
    if (x.isPositiveInfinity) return@fn atan(PI / 2).toNormal()
    if (x.isNegativeInfinity) return@fn atan(-PI / 2).toNormal()
    atan(x.value)
        .toNormal()
}

@EsSpec("Math.atanh")
val atanh = functionWithoutThis("atanh", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero) return@fn x.toNormal()
    if (x.value > 1.0 || x.value < -1.0) return@fn NumberType.NaN.toNormal()
    if (x.value == 1.0) return@fn NumberType.POSITIVE_INFINITY.toNormal()
    if (x.value == -1.0) return@fn NumberType.NEGATIVE_INFINITY.toNormal()
    asin(x.value)
        .toNormal()
}

@EsSpec("Math.atan2")
val atan2 = functionWithoutThis("atan2", 2u) fn@ { args ->
    val y = args[0]
        .requireToBe<NumberType> { return@fn it }
    val x = args[1]
        .requireToBe<NumberType> { return@fn it }
    if (y.isNaN || x.isNaN) return@fn NumberType.NaN.toNormal()
    atan2(y.value, x.value)
        .toNormal()
}

@EsSpec("Math.cbrt")
val cbrt = functionWithoutThis("cbrt", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isZero) return@fn x.toNormal()
    cbrt(x.value)
        .toNormal()
}

@EsSpec("Math.ceil")
val ceil = functionWithoutThis("ceil", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isZero) return@fn x.toNormal()
    ceil(x.value)
        .toNormal()
}

@EsSpec("Math.clz32")
val clz32 = functionWithoutThis("clz32", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
        .toUint32()
        .orReturnThrow { return@fn it }
    x.value.toUInt().countLeadingZeroBits()
        .languageValue
        .toNormal()
}

@EsSpec("Math.cos")
val cos = functionWithoutThis("cos", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite }) return@fn NumberType.NaN.toNormal()
    if (x.isZero) return@fn 1.0.toNormal()
    cos(x.value)
        .toNormal()
}

@EsSpec("Math.cosh")
val cosh = functionWithoutThis("cosh", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN) return@fn NumberType.NaN.toNormal()
    if (x.isInfinity) return@fn NumberType.POSITIVE_INFINITY.toNormal()
    if (x.isZero) return@fn 1.0.toNormal()
    cosh(x.value)
        .toNormal()
}

@EsSpec("Math.exp")
val exp = functionWithoutThis("exp", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isPositiveInfinity) return@fn x.toNormal()
    if (x.isZero) return@fn 1.0.toNormal()
    if (x.isNegativeInfinity) return@fn NumberType.POSITIVE_ZERO.toNormal()
    exp(x.value)
        .toNormal()
}

@EsSpec("Math.floor")
val floor = functionWithoutThis("floor", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero || x.isPositiveInfinity) return@fn x.toNormal()
    if (x.isNegativeInfinity) return@fn (-1.0).toNormal()
    floor(x.value)
        .toNormal()
}

@EsSpec("Math.hypot")
val hypot = functionWithoutThis("hypot", 1u) fn@ { args ->
    args.forEach { it.requireToBe<NumberType> { return@fn it } }
    (args as List<NumberType>).reduce { acc, it ->
        hypot(acc.value, it.value)
            .languageValue
    }
        .toNormal()
}

@EsSpec("Math.imul")
val imul = functionWithoutThis("imul", 2u) fn@ { args ->
    val a = args[0]
        .requireToBe<NumberType> { return@fn it }
        .toInt32()
        .orReturnThrow { return@fn it }
        .value
        .toInt()
    val b = args[1]
        .requireToBe<NumberType> { return@fn it }
        .toInt32()
        .orReturnThrow { return@fn it }
        .value
        .toInt()
    (a * b)
        .languageValue
        .toNormal()
}

@EsSpec("Math.log")
val ln = functionWithoutThis("ln", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    when {
        x.isNaN || x.isPositiveInfinity -> x
        x.value == 1.0 -> NumberType.POSITIVE_ZERO
        x.isZero -> NumberType.NEGATIVE_INFINITY
        x.value < 0.0 -> NumberType.NaN
        else -> ln(x.value).languageValue
    }
        .toNormal()
}

val mathLog = functionWithoutThis("log", 2u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    val base = args[1]
        .requireToBe<NumberType> { return@fn it }
    when {
        x.isNaN || base.isNaN
         || x.value < 0.0 || base.value <= 0.0 || base.value == 1.0
         || x.isPositiveInfinity && base.isPositiveInfinity
         -> NumberType.NaN
        x.isPositiveInfinity && base.value > 1.0 -> NumberType.POSITIVE_ZERO
        x.isPositiveInfinity && base.value < 1.0 -> NumberType.NEGATIVE_INFINITY
        x.isZero && base.value > 1.0 -> NumberType.NEGATIVE_INFINITY
        x.isZero && base.value < 1.0 -> NumberType.POSITIVE_INFINITY
        else -> (ln(x.value) / ln(base.value)).languageValue
    }
        .toNormal()
}

private fun generateLogN(n: Int, fn: (Double) -> Double) =
    functionWithoutThis("log$n", 1u) fn@ { args ->
        val x = args[0]
            .requireToBe<NumberType> { return@fn it }
        when {
            x.isNaN || x.isPositiveInfinity -> x
            x.isZero -> NumberType.NEGATIVE_INFINITY
            x.value < 0.0 -> NumberType.NaN
            else -> fn(x.value).languageValue
        }
            .toNormal()
    }
@EsSpec("Math.log10")
val log10 = generateLogN(10, ::kotlinLog10)
@EsSpec("Math.log2")
val log2 = generateLogN(2, ::kotlinLog2)

@EsSpec("Math.max")
val max = functionWithoutThis("max", 1u) fn@ { args ->
    var highest = NumberType.NEGATIVE_INFINITY
    args.forEach {
        val number = it.requireToBe<NumberType> { return@fn it }
        if (number.isNaN) return@fn throwError(TypeErrorKind.CANNOT_COMPARE_NAN)
        if (number.value > highest.value) highest = number
    }
    highest.toNormal()
}

@EsSpec("Math.min")
val min = functionWithoutThis("min", 1u) fn@ { args ->
    var lowest = NumberType.POSITIVE_INFINITY
    args.forEach {
        val number = it.requireToBe<NumberType> { return@fn it }
        if (number.isNaN) return@fn throwError(TypeErrorKind.CANNOT_COMPARE_NAN)
        if (number.value < lowest.value) lowest = number
    }
    lowest.toNormal()
}

@EsSpec("Math.pow")
val power = functionWithoutThis("power", 2u) fn@ { args ->
    val base = args[0]
        .requireToBe<NumberType> { return@fn it }
    val exponent = args[1]
        .requireToBe<NumberType> { return@fn it }
    base.pow(exponent)
}

@EsSpec("Math.random")
val random = functionWithoutThis("random") fn@ { _ ->
    Random.nextDouble().toNormal()
}

@EsSpec("Math.round")
val round = functionWithoutThis("round", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isInteger) return@fn x.toNormal()
    if (x.value < 0.5 && x.value > 0.0) return@fn NumberType.POSITIVE_ZERO.toNormal()
    if (x.value < 0.0 && x.value >= -0.5) return@fn NumberType.NEGATIVE_ZERO.toNormal()
    round(x.value)
        .toNormal()
}

@EsSpec("Math.fround")
val roundToFloat = functionWithoutThis("roundToFloat", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isZero) return@fn x.toNormal()
    x.value.toFloat().toDouble()
        .toNormal()
}

@EsSpec("Math.sign")
val sign = functionWithoutThis("sign", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN) return@fn x.toNormal()
    // since -0 exists, +0 is positive and -0 is negative
    (
        if (x.isNegative) -1.0
        else 1.0
    )
        .toNormal()
}

@EsSpec("Math.sin")
val sin = functionWithoutThis("sin", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero) return@fn x.toNormal()
    if (x.isInfinity) return@fn NumberType.NaN.toNormal()
    sin(x.value)
        .toNormal()
}

@EsSpec("Math.sinh")
val sinh = functionWithoutThis("sinh", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isZero) return@fn x.toNormal()
    sinh(x.value)
        .toNormal()
}

@EsSpec("Math.sqrt")
val sqrt = functionWithoutThis("sqrt", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero || x.isPositiveInfinity) return@fn x.toNormal()
    if (x.isNegative) return@fn NumberType.NaN.toNormal()
    sqrt(x.value)
        .toNormal()
}

@EsSpec("Math.tan")
val tan = functionWithoutThis("tan", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero) return@fn x.toNormal()
    if (x.isInfinity) return@fn NumberType.NaN.toNormal()
    tan(x.value)
        .toNormal()
}

@EsSpec("Math.tanh")
val tanh = functionWithoutThis("tanh", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.isNaN || x.isZero) return@fn x.toNormal()
    if (x.isInfinity) return@fn 1.0.withSign(x.value).toNormal()
    tan(x.value)
        .toNormal()
}

@EsSpec("Math.truncate")
val truncate = functionWithoutThis("truncate", 1u) fn@ { args ->
    val x = args[0]
        .requireToBe<NumberType> { return@fn it }
    if (x.not { isFinite } || x.isZero) return@fn x.toNormal()
    truncate(x.value)
        .toNormal()
}

@EsSpec("%Math%")
val Math = ObjectType(properties=mutableMapOf(
    "E".sealedData(E.languageValue),
    "LN_10".sealedData(ln(10.0).languageValue),
    "LN_2".sealedData(ln(2.0).languageValue),
    "LOG_10_E".sealedData(kotlinLog10(E).languageValue),
    "LOG_2_E".sealedData(kotlinLog2(E).languageValue),
    "PI".sealedData(PI.languageValue),
    "SQRT_1_2".sealedData(sqrt(0.5).languageValue),
    "SQRT_2".sealedData(sqrt(2.0).languageValue),

    sealedMethod(abs),
    sealedMethod(acos),
    sealedMethod(acosh),
    sealedMethod(asin),
    sealedMethod(asinh),
    sealedMethod(atan),
    sealedMethod(atanh),
    sealedMethod(atan2),
    sealedMethod(cbrt),
    sealedMethod(ceil),
    sealedMethod(clz32),
    sealedMethod(cos),
    sealedMethod(cosh),
    sealedMethod(exp),
    sealedMethod(floor),
    sealedMethod(hypot),
    sealedMethod(imul),
    sealedMethod(ln),
    sealedMethod(mathLog),
    sealedMethod(log10),
    sealedMethod(log2),
    sealedMethod(max),
    sealedMethod(min),
    sealedMethod(power),
    sealedMethod(random),
    sealedMethod(round),
    sealedMethod(roundToFloat),
    sealedMethod(sign),
    sealedMethod(sin),
    sealedMethod(sinh),
    sealedMethod(sqrt),
    sealedMethod(tan),
    sealedMethod(tanh),
    sealedMethod(truncate),
))

private fun Double.toNormal() =
    languageValue.toNormal()
