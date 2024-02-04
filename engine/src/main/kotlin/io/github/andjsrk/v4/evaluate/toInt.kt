package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

internal fun NumberType.toInt(moduloValue: Long): MaybeThrow<NumberType> =
    this.toUint(moduloValue) {
        if (it > (moduloValue shr 1) - 1) it - moduloValue
        else it
    }
