package io.github.andjsrk.v4.evaluate.type

sealed interface PrimitiveLanguageType: LanguageType {
    val nativeValue: Any?
}
