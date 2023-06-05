package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.not
import kotlin.math.truncate

@EsSpec("ToUint32")
internal fun NumberType.toUint32(): Completion {
    if (this.not { isFinite }) return Completion(Completion.Type.THROW, NullType/* RangeError */)
    if (this.isZero) return Completion.normal(NumberType.POSITIVE_ZERO)
    val intPart = truncate(value)
    val intPart32Bit = intPart modulo UInt.MAX_VALUE.toLong()
    return Completion.normal(NumberType(intPart32Bit))
}
