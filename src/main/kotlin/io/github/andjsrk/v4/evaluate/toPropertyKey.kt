package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

@EsSpec("ToPropertyKey")
internal fun LanguageType.toPropertyKey(): MaybeAbrupt<PropertyKey> =
    if (this is SymbolType) this.toNormal()
    else stringify(this)
