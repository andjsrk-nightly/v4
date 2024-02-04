package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

@EsSpec("ToUint32")
internal fun NumberType.toUint32(): MaybeThrow<NumberType> =
    this.toUint(1L shl 32)
