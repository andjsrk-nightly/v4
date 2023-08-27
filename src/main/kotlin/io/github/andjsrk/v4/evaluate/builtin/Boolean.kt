package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.not
import java.math.BigInteger

@EsSpec("Boolean(value)")
private val booleanFrom = functionWithoutThis("from", 1u) fn@ { args ->
    val value = args[0]
    when (value) {
        NullType -> false
        is StringType -> value.value.isNotEmpty()
        is NumberType -> value.not { isZero } && value.not { isNaN }
        is BooleanType -> return@fn value.toNormal()
        is BigIntType -> value.value != BigInteger.ZERO
        is ObjectType -> return@fn throwError(TypeErrorKind.BOOLEAN_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
        is SymbolType -> return@fn throwError(TypeErrorKind.BOOLEAN_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
    }
        .languageValue
        .toNormal()
}

private val booleanToString = method(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val boolean = thisArg.requireToBe<BooleanType> { return@fn it }
    boolean.value.toString()
        .languageValue
        .toNormal()
}

val Boolean = BuiltinClassType(
    "Boolean",
    Object,
    mutableMapOf(
        sealedMethod(booleanFrom),
    ),
    mutableMapOf(
        sealedMethod(booleanToString),
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Boolean")
    },
)
