package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal inline fun Completion<*>.returnIfShouldNotContinue(res: LanguageType, `return`: AbruptReturnLambda): LanguageType {
    if (!continueLoop(this)) {
        require(this is Completion.Abrupt)
        `return`(updateEmpty(this, res))
    }
    return value as LanguageType? ?: res
}
