package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.Array

class ImmutableArrayType(
    override val array: List<LanguageType>,
    origin: List<LanguageType>? = null,
): ArrayType(lazy { Array.instancePrototype }) {
    init {
        if (origin != null) {
            TODO()
        }
    }

    companion object {
        fun from(collection: Collection<LanguageType>) =
            ImmutableArrayType(collection.toList())
    }
}
