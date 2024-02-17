package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.ExecutionContext
import io.github.andjsrk.v4.evaluate.SimpleLazyFlow
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.EmptyOrThrow

sealed class GeneratorType<S: Enum<*>>(
    lazyPrototype: Lazy<io.github.andjsrk.v4.evaluate.type.PrototypeObjectType>,
): io.github.andjsrk.v4.evaluate.type.ObjectType(lazyPrototype) {
    abstract val context: ExecutionContext // [[GeneratorContext]]
    abstract var state: S?
    abstract val brand: String? // [[GeneratorBrand]]
    abstract fun start(result: SimpleLazyFlow<Completion.FromFunctionBody<*>>)
    abstract fun validate(brand: String? = null): EmptyOrThrow
}
