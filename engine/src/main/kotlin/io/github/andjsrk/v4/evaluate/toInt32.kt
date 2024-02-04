package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

@EsSpec("ToInt32")
internal fun NumberType.toInt32(): MaybeThrow<NumberType> =
    this.toInt(1L shl 32)
