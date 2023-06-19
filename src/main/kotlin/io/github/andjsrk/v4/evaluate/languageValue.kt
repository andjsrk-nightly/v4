package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

/**
 * Converts from [String] to [StringType] without parentheses.
 */
internal inline val String.languageValue get() =
    StringType(this)

internal inline val Double.languageValue get() =
    NumberType(this)
