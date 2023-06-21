package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.Array
import io.github.andjsrk.v4.evaluate.languageValue

@EsSpec("Array Objects")
@EsSpec("ArrayCreate")
class ArrayType(var length: Long, origin: ArrayType? = null): ObjectType(Array.instancePrototype) {
    init {
        if (origin != null) {
            TODO()
        } else {
            for (i in 0..length) createDataProperty(i.toString().languageValue, NullType)
        }
    }
}
