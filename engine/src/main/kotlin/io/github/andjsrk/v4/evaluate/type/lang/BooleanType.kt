package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.display

@JvmInline
value class BooleanType private constructor(override val value: Boolean): PrimitiveLanguageType {
    operator fun not() =
        BooleanType.from(!value)
    infix fun or(other: BooleanType) =
        BooleanType.from(value || other.value)

    override fun toString() = display()

    companion object {
        val TRUE = BooleanType(true)
        val FALSE = BooleanType(false)
        fun from(value: Boolean) =
            if (value) TRUE
            else FALSE
    }
}
