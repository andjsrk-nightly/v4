package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.not
import kotlin.math.truncate

/**
 * An extraction of common code for `ToIntN`/`ToUintN`
 */
internal inline fun NumberType.toUint(moduloValue: Long, transform: (Double) -> Double = { it }): MaybeAbrupt<NumberType> {
    if (this.not { isFinite }) return unexpectedRange(null, "finite")
    if (this.isZero) return NumberType.POSITIVE_ZERO.toNormal()
    val intPart = truncate(value)
    val intPartBits = intPart.mod(moduloValue.toDouble())
    return transform(intPartBits)
        .languageValue
        .toNormal()
}
