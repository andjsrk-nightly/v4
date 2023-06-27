package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.isInteger

val isInteger = BuiltinFunctionType("isInteger", 1u) { _, args ->
    val number = args[0]
    Completion.Normal(
        BooleanType.from(
            number is NumberType && number.isFinite && number.value.isInteger
        )
    )
}
