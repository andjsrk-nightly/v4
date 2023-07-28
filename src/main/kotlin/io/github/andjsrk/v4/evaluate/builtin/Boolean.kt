package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.not
import java.math.BigInteger

@EsSpec("Boolean(value)")
private val booleanFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val value = args[0]
    Completion.Normal(
        when (value) {
            NullType -> false
            is StringType -> value.value.isNotEmpty()
            is NumberType -> value.not { isZero } && value.not { isNaN }
            is BooleanType -> return@fn Completion.Normal(value)
            is BigIntType -> value.value != BigInteger.ZERO
            is ObjectType -> return@fn throwError(TypeErrorKind.BOOLEAN_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
            is SymbolType -> return@fn throwError(TypeErrorKind.BOOLEAN_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
        }
            .languageValue
    )
}

private val booleanToString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val boolean = thisArg.requireToBe<BooleanType> { return@fn it }
    Completion.Normal(
        boolean.value.toString().languageValue
    )
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
