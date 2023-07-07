package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import java.math.BigInteger

/**
 * Converts from [String] to [StringType] without parentheses.
 */
internal inline val String.languageValue get() =
    StringType(this)

/**
 * Converts from [Double] to [NumberType] without parentheses.
 */
internal inline val Double.languageValue get() =
    NumberType(this)
/**
 * Converts from [Number] to [NumberType] without parentheses.
 */
internal inline val Number.languageValue get() =
    this.toDouble().languageValue

/**
 * Converts from [BigInteger] to [BigIntType] without parentheses.
 */
internal inline val BigInteger.languageValue get() =
    BigIntType(this)

/**
 * Converts from [Boolean] to [BooleanType] without parentheses.
 */
internal inline val Boolean.languageValue get() =
    BooleanType.from(this)
