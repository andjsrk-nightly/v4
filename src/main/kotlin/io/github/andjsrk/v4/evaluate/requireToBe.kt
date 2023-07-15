package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

internal inline fun <reified T: LanguageType> LanguageType?.requireToBe(`return`: AbruptReturnLambda): T {
    if (this !is T) `return`(unexpectedType(this, T::class))
    return this
}

internal inline fun LanguageType.requireToBeString(`return`: AbruptReturnLambda) =
    requireToBe<StringType>(`return`)
        .value
