package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.display

@JvmInline
value class BooleanType private constructor(override val nativeValue: Boolean): PrimitiveLanguageType {
    operator fun not() =
        BooleanType.from(!nativeValue)
    infix fun or(other: BooleanType) =
        BooleanType.from(nativeValue || other.nativeValue)

    override fun toString() = display()

    companion object {
        val TRUE = BooleanType(true)
        val FALSE = BooleanType(false)
        fun from(value: Boolean) =
            if (value) TRUE
            else FALSE
    }
}
