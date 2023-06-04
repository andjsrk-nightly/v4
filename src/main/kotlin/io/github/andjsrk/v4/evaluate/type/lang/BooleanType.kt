package io.github.andjsrk.v4.evaluate.type.lang

@JvmInline
value class BooleanType private constructor(override val value: Boolean): LanguageType {
    operator fun not() =
        BooleanType.from(!value)

    companion object {
        val TRUE = BooleanType(true)
        val FALSE = BooleanType(false)
        fun from(value: Boolean) =
            if (value) TRUE
            else FALSE
    }
}
