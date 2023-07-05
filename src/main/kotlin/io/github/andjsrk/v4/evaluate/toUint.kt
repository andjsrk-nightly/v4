package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.not
import kotlin.math.truncate

/**
 * An extraction of common code for `ToIntN`/`ToUintN`
 */
internal inline fun NumberType.toUint(moduloValue: Long, transform: (Double) -> Double = { it }): MaybeAbrupt<NumberType> {
    if (this.not { isFinite }) return throwError(RangeErrorKind.MUST_BE_FINITE)
    if (this.isZero) return Completion.Normal(NumberType.POSITIVE_ZERO)
    val intPart = truncate(value)
    val intPartBits = intPart.mod(moduloValue.toDouble())
    return Completion.Normal(transform(intPartBits).languageValue)
}
