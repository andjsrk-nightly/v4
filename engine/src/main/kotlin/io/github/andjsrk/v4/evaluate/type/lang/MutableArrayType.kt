package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.MutableArray

class MutableArrayType(override val array: MutableList<LanguageType>): ArrayType(lazy { MutableArray.instancePrototype }) {
    companion object {
        fun from(collection: Collection<LanguageType>) =
            ImmutableArrayType(collection.toMutableList())
    }
}
