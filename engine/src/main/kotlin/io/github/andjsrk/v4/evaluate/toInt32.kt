package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.NumberType

@EsSpec("ToInt32")
internal fun NumberType.toInt32(): @CompilerFalsePositive MaybeThrow<NumberType> =
    this.toInt(1L shl 32)
