package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal inline fun <reified T: LanguageType> LanguageType?.requireToBe(`return`: AbruptReturnLambda): T {
    if (this !is T) `return`(createThrow<T>(this))
    return this
}

internal inline fun <reified T: LanguageType> LanguageType?.requireToBeNullable(`return`: AbruptReturnLambda): T? {
    if (this !is T?) `return`(createThrow<T>(this))
    return this
}

private inline fun <reified Expected: LanguageType> createThrow(actualValue: LanguageType?) =
    throwError(
        TypeErrorKind.UNEXPECTED_TYPE,
        generalizedDescriptionOf<Expected>(),
        actualValue?.let { generalizedDescriptionOf(it) } ?: "nothing"
    )
