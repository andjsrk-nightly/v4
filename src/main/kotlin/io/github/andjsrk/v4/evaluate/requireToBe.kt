package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

internal inline fun <reified T: LanguageType?> LanguageType?.requireToBe(`return`: AbruptReturnLambda): T {
    if (this !is T) `return`(Completion.Throw(NullType/* TypeError */))
    return this
}
