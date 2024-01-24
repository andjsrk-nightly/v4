package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.ExecutionContext
import io.github.andjsrk.v4.evaluate.SimpleLazyFlow
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.MaybeEmptyOrAbrupt

sealed class GeneratorType<S: Enum<*>>(
    lazyPrototype: Lazy<PrototypeObjectType>,
): ObjectType(lazyPrototype) {
    abstract val context: ExecutionContext // [[GeneratorContext]]
    abstract var state: S?
    abstract val brand: String? // [[GeneratorBrand]]
    abstract fun start(result: SimpleLazyFlow<MaybeEmptyOrAbrupt>)
    abstract fun validate(brand: String? = null): EmptyOrAbrupt
}
