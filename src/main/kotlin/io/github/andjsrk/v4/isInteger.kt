package io.github.andjsrk.v4

import kotlin.math.truncate

internal inline val Double.isInteger get() =
    this == truncate(this)
