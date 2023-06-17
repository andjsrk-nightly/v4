package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.Completion

internal inline fun Completion.returnIfShouldNotContinue(res: LanguageType, `return`: CompletionReturn): LanguageType {
    if (!continuesLoop) `return`(updateEmpty(this, res))
    return languageValue ?: res
}
