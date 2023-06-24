package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.sameValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ClassType

val Object = ClassType(
    null,
    mutableMapOf(
        "is".languageValue to DataProperty.sealed(
            BuiltinFunctionType("name".languageValue, 2u) { _, args ->
                Completion.Normal(sameValue(args[0], args[1]))
            }
        ),
        // TODO
    ),
) {

}
