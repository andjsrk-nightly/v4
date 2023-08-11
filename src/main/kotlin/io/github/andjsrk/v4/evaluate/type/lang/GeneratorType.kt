package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.ExecutionContext

sealed class GeneratorType<S: Enum<*>>(
    lazyPrototype: Lazy<PrototypeObjectType>,
): ObjectType(lazyPrototype) {
    abstract var context: ExecutionContext // [[GeneratorContext]]
    abstract var state: S?
    abstract val brand: String? // [[GeneratorBrand]]
    abstract fun start(createResult: () -> EvalFlow<LanguageType?>)
}
