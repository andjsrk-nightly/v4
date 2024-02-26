package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.display
import io.github.andjsrk.v4.evaluate.languageValue

/**
 * Note that this covers a [Private Name](https://tc39.es/ecma262/multipage/ecmascript-data-types-and-values.html#sec-private-names).
 * @see [isPrivateName]
 */
@JvmInline
value class StringType(override val nativeValue: String): PrimitiveLanguageType, LanguageTypePropertyKey {
    operator fun plus(other: StringType) =
        StringType(nativeValue + other.nativeValue)
    fun lessThan(other: StringType) =
        (nativeValue < other.nativeValue)
            .languageValue
            .toNormal()
    val isPrivateName get() =
        nativeValue.startsWith('#')

    override fun toString() = display()

    companion object {
        val empty = StringType("")
    }
}
