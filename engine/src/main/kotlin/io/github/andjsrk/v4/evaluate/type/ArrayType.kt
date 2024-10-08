package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

@EsSpec("Array Objects")
@EsSpec("ArrayCreate")
sealed class ArrayType(lazyPrototype: Lazy<PrototypeObjectType>): ObjectType by ObjectType.Impl(lazyPrototype) {
    abstract val array: List<LanguageType>
}
