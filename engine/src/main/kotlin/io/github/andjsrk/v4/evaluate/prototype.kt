package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*

val LanguageType.prototype get() =
    when (this) {
        is ObjectType -> prototype
        else -> {
            require(this is PrimitiveLanguageType)
            prototype
        }
    }

val PrimitiveLanguageType.prototype get() =
    toBuiltinClass()?.instancePrototype
