package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.StringType

/**
 * Converts from [String] to [StringType] without parentheses.
 */
inline val String.languageValue get() =
    StringType(this)
