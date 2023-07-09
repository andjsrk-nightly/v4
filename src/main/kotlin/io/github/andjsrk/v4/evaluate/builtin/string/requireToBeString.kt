package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.AbruptReturnLambda
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

internal inline fun LanguageType.requireToBeString(`return`: AbruptReturnLambda) =
    requireToBe<StringType>(`return`)
        .value
