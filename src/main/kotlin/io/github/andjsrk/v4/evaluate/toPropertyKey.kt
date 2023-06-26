package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("ToPropertyKey")
internal fun LanguageType.toPropertyKey(): MaybeAbrupt<PropertyKey> =
    if (this is SymbolType) Completion.Normal(this)
    else stringify(this)
