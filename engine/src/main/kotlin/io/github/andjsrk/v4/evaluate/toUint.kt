package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.not
import kotlin.math.truncate

/**
 * An extraction of common code for `ToIntN`/`ToUintN`
 */
internal inline fun NumberType.toUint(moduloValue: Long, transform: (Double) -> Double = { it }): MaybeThrow<NumberType> {
    if (this.not { isFinite }) return unexpectedNumberRange(null, "finite")
    if (this.isZero) return NumberType.POSITIVE_ZERO.toNormal()
    val intPart = truncate(value)
    val intPartBits = intPart.mod(moduloValue.toDouble())
    return transform(intPartBits)
        .languageValue
        .toNormal()
}
