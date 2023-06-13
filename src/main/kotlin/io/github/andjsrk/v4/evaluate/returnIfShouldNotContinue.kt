package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

internal inline fun Completion.returnIfShouldNotContinue(res: LanguageType, `return`: CompletionReturn): LanguageType {
    if (!continuesLoop) `return`(updateEmpty(this, res))
    val bodyValue = neverAbrupt<LanguageType>(this)
    return if (this.isEmpty) res else bodyValue
}
