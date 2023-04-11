package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.Location

data class TokenizerError(val message: String, val location: Location)
