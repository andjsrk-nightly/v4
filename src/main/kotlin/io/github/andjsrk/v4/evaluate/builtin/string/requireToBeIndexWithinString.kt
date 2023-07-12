package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.AbruptReturnLambda
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.requireToBeIndex

internal inline fun NumberType.requireToBeIndexWithinString(string: String, name: String = "startIndex", `return`: AbruptReturnLambda): Int {
    val index = this.requireToBeIndex(`return`)
    if (index >= string.length) `return`(
        throwError(
            RangeErrorKind.MUST_BE_INTEGER_IN_RANGE,
            name,
            "0",
            "(length of the string) - 1",
        )
    )
    return index
}
