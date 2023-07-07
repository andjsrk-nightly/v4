package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal fun List<LanguageType>.getOptional(index: Int) =
    getOrNull(index).normalizeNull()
