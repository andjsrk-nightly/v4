package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.display
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.toNormal

/**
 * Note that this covers a [Private Name](https://tc39.es/ecma262/multipage/ecmascript-data-types-and-values.html#sec-private-names).
 * @see [isPrivateName]
 */
@JvmInline
value class StringType(override val value: String): PrimitiveLanguageType, LanguageTypePropertyKey {
    operator fun plus(other: StringType) =
        StringType(value + other.value)
    fun lessThan(other: StringType) =
        (value < other.value)
            .languageValue
            .toNormal()
    val isPrivateName get() =
        value.startsWith('#')

    override fun toString() = display()

    companion object {
        val empty = StringType("")
    }
}
