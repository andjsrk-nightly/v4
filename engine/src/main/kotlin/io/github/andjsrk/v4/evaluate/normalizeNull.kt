package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.LanguageType
import io.github.andjsrk.v4.evaluate.type.NullType

/**
 * Normalizes [NullType] to `null`.
 */
internal fun <V: LanguageType> V.normalizeNull() =
    takeIf { this != NullType }
