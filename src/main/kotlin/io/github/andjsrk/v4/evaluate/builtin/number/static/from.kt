package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parseNumber

@EsSpec("Number(value)") // (as a normal function)
val from = BuiltinFunctionType("from", 1u) fn@ { _, args ->
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
