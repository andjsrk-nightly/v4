package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

internal inline fun AbstractType/* LanguageTypeOrCompletion */.extractIfCompletion(`return`: (Completion) -> Nothing) =
    if (this is Completion) returnIfAbrupt<LanguageType>(this, `return`)
    else this as LanguageType
