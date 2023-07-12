package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.AbruptReturnLambda
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.requireToBeUnsignedInt

internal inline fun NumberType.requireToBePositionWithinString(string: String, name: String = "stringEnd", `return`: AbruptReturnLambda): Int {
    val index = this.requireToBeUnsignedInt(`return`)
    if (index > string.length) `return`(
        throwError(
            RangeErrorKind.MUST_BE_INTEGER_IN_RANGE,
            name,
            "0",
            "length of the string",
        )
    )
    return index
}
