package io.github.andjsrk.v4.evaluate

import kotlin.math.truncate

internal val Double.isInteger get() =
    this == truncate(this)
