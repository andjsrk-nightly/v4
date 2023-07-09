package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

internal inline fun LanguageType.requireToBeStartIndex(string: String, paramName: String = "startIndex", `return`: AbruptReturnLambda): Int {
    val index = this
        .requireToBe<NumberType>(`return`)
        .requireToBeIndex(`return`)
    if (index > string.length) `return`(
        throwError(
            RangeErrorKind.MUST_BE_INTEGER_IN_RANGE,
            paramName,
            "0",
            "length of the string",
        )
    )
    return index
}
