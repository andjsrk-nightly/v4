package io.github.andjsrk.v4.evaluate.type.lang

sealed interface PrimitiveLanguageType: LanguageType {
    val value: Any?
}
