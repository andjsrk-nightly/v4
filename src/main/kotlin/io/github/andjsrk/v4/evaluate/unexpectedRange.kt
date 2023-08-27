package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.RangeErrorKind

internal fun unexpectedRange(
    argName: String?,
    expectedRange: String,
) =
    throwError(RangeErrorKind.NUMBER_MUST_BE, argName ?: "The number", expectedRange)
