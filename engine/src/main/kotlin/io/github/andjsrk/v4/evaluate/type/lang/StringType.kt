package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.display
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.toNormal

@JvmInline
value class StringType(override val value: String): PrimitiveLanguageType, PropertyKey {
    operator fun plus(other: StringType) =
        StringType(value + other.value)
    fun lessThan(other: StringType) =
        (value < other.value)
            .languageValue
            .toNormal()

    override fun toString() = display()

    companion object {
        val empty = StringType("")
    }
}
