package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

internal inline fun <reified T: LanguageType> LanguageType?.requireToBe(rtn: AbruptReturnLambda): T {
    if (this !is T) rtn(unexpectedType(this, T::class))
    return this
}

internal inline fun LanguageType.requireToBeString(rtn: AbruptReturnLambda) =
    requireToBe<StringType>(rtn)
        .value
