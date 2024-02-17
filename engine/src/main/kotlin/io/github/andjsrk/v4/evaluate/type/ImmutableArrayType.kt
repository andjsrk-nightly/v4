package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.builtin.Array

data class ImmutableArrayType(override val array: List<LanguageType>): ArrayType(lazy { Array.instancePrototype }) {
    companion object {
        fun from(collection: Collection<LanguageType>) =
            ImmutableArrayType(collection.toList())
    }
}
