package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.Array

class ImmutableArrayType(
    array: List<LanguageType>,
    origin: List<LanguageType>? = null,
): ArrayType(lazy { Array.instancePrototype }) {
    override val array: List<LanguageType> =
        if (origin != null) TODO()
        else array

    companion object {
        fun from(collection: Collection<LanguageType>) =
            ImmutableArrayType(collection.toList())
    }
}
