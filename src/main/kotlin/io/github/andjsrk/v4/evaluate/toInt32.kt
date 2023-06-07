package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

@EsSpec("ToInt32")
internal fun NumberType.toInt32() =
    this.toInt(1L shl 32)
