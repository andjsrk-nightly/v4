package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.NumberType

@EsSpec("ToUint32")
internal fun NumberType.toUint32(): @CompilerFalsePositive MaybeThrow<NumberType> =
    this.toUint(1L shl 32)
