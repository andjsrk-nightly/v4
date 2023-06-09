package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

internal inline fun AbstractType/* LanguageType or Completion */.extractFromCompletionOrReturn(`return`: CompletionReturn) =
    if (this is Completion) getLanguageTypeOrReturn(this, `return`)
    else this as LanguageType
