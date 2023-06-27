package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.isInteger
import kotlin.math.abs

val isSafeInteger = BuiltinFunctionType("isSafeInteger", 1u) { _, args ->
    val number = args[0]
    Completion.Normal(
        BooleanType.from(
            number is NumberType
                && number.value.isInteger
                && abs(number.value) <= NumberType.MAX_SAFE_INTEGER
        )
    )
}
