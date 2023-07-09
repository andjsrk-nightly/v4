package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal inline fun <reified T: LanguageType> LanguageType?.requireToBe(`return`: AbruptReturnLambda): T {
    if (this !is T) `return`(
        throwError(
            TypeErrorKind.UNEXPECTED_TYPE,
            generalizedDescriptionOf<T>(),
            this?.let { generalizedDescriptionOf(it::class) } ?: "nothing"
        )
    )
    return this
}
