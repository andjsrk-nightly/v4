package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.MutableArray

class MutableArrayType(
    override val array: MutableList<LanguageType>,
    origin: List<LanguageType>? = null,
): ArrayType(lazy { MutableArray.instancePrototype }) {
    init {
        if (origin != null) {
            TODO()
        }
    }

    companion object {
        fun from(collection: Collection<LanguageType>) =
            ImmutableArrayType(collection.toMutableList())
    }
}
