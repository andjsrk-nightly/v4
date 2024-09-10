package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*

val LanguageType.prototype get() =
    when (this) {
        is ObjectType -> prototype
        is PrimitiveLanguageType -> prototype
    }

val PrimitiveLanguageType.prototype get() =
    associatedClass?.instancePrototype
