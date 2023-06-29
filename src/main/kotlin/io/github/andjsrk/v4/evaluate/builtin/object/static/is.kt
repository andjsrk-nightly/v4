package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.sameValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType

@EsSpec("Object.is")
val `is` = BuiltinFunctionType("is", 2u) { _, args ->
    Completion.Normal(sameValue(args[0], args[1]))
}
