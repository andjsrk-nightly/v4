package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.not
import kotlin.math.truncate

/**
 * An extraction of common code for `ToIntN`/`ToUintN`
 */
internal inline fun NumberType.toUint(moduloValue: Long, transform: (Double) -> Double = { it }): Completion {
    if (this.not { isFinite }) return Completion.`throw`(NullType/* RangeError */)
    if (this.isZero) return Completion.normal(NumberType.POSITIVE_ZERO)
    val intPart = truncate(value)
    val intPartBits = intPart.mod(moduloValue.toDouble())
    return Completion.normal(NumberType(transform(intPartBits)))
}
