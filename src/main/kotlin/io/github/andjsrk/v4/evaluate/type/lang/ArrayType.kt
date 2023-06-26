package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Array
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt

@EsSpec("Array Objects")
@EsSpec("ArrayCreate")
class ArrayType(var length: Long, origin: ArrayType? = null): ObjectType(Array.instancePrototype) {
    init {
        if (origin != null) {
            TODO()
        } else {
            for (i in 0..length) initializeAt(i, NullType)
        }
    }
    internal fun initializeAt(index: Long, value: LanguageType): EmptyOrAbrupt {
        val indexKey = neverAbrupt(stringify(index.toDouble().languageValue))
        return createDataPropertyOrThrow(indexKey, value)
    }

    companion object {
        fun from(collection: Collection<LanguageType>) =
            ArrayType(collection.size.toLong()).apply {
                collection.forEachIndexed { i, value ->
                    initializeAt(i.toLong(), value)
                }
            }
    }
}
