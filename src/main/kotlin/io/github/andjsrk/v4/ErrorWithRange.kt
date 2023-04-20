package io.github.andjsrk.v4

import io.github.andjsrk.v4.error.Error

data class ErrorWithRange(val kind: Error, val range: Range)
