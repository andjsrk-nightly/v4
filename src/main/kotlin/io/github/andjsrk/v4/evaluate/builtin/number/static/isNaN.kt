package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Number.isNaN")
val isNaN = BuiltinFunctionType("isNaN", 1u) { _, args ->
    Completion.Normal(
        BooleanType.from(
            args[0] == NumberType.NaN
        )
    )
}
