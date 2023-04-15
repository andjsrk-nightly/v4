package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.Location
import io.github.andjsrk.v4.error.Error

data class TokenizerError(val kind: Error, val location: Location)
