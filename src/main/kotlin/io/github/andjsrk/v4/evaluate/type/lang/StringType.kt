package io.github.andjsrk.v4.evaluate.type.lang

@JvmInline
value class StringType(override val value: String): LanguageType {
    operator fun plus(other: StringType) =
        StringType(value + other.value)
    fun lessThan(other: StringType) {
        TODO()
    }
}
