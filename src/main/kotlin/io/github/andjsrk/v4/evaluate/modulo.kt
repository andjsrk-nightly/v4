package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import kotlin.math.withSign

@EsSpec("modulo")
internal infix fun Double.modulo(other: Long) =
    (this % other).withSign(other.toDouble())
