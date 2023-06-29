package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal inline fun AbstractType/* LanguageType or Completion */.extractFromCompletionOrReturn(`return`: AbruptReturnLambda) =
    if (this is Completion<*>) returnIfAbrupt(this, `return`) as LanguageType
    else this as LanguageType
