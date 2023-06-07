package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.NumberType

internal fun NumberType.toInt(moduloValue: Long) =
    this.toUint(moduloValue) {
        if (it > (moduloValue shr 1) - 1) it - moduloValue
        else it
    }
