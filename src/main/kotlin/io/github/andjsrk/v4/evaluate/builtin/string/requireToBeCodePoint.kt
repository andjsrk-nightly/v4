package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.AbruptReturnLambda
import io.github.andjsrk.v4.evaluate.Ranges
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.requireToBeIntegerWithin

internal inline fun NumberType.requireToBeCodePoint(`return`: AbruptReturnLambda) =
    requireToBeIntegerWithin(Ranges.codePoint, "A code point", `return`)
        .toInt()
