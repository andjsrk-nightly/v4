package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.ExecutionContext
import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt

sealed class GeneratorType<S: Enum<*>>(
    lazyPrototype: Lazy<PrototypeObjectType>,
): ObjectType(lazyPrototype) {
    abstract var context: ExecutionContext // [[GeneratorContext]]
    abstract var state: S?
    abstract val brand: String? // [[GeneratorBrand]]
    abstract fun start(result: Sequence<NormalOrAbrupt>)
}
