package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.builtin.MutableArray

data class MutableArrayType(override var array: MutableList<LanguageType>): ArrayType(lazy { MutableArray.instancePrototype }) {
    companion object {
        fun from(collection: Collection<LanguageType>) =
            ImmutableArrayType(collection.toMutableList())
    }
}
