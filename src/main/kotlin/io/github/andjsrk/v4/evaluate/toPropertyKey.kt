package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

@EsSpec("ToPropertyKey")
internal fun LanguageType.toPropertyKey() =
    if (this is SymbolType) Completion.Normal(this)
    else toString(this)
