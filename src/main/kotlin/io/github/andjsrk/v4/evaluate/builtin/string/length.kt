package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBeString
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion

val lengthGetter = AccessorProperty.builtinGetter("length") fn@ {
    val string = it.requireToBeString { return@fn it }
    Completion.Normal(
        string.length.languageValue
    )
}
