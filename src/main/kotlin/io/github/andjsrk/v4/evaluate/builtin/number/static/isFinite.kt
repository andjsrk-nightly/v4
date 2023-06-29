package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Number.isFinite")
val isFinite = BuiltinFunctionType("isFinite", 1u) { _, args ->
    val number = args[0]
    Completion.Normal(
        BooleanType.from(
            number is NumberType && number.isFinite
        )
    )
}
