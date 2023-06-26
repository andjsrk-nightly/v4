package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("accessor property")
data class AccessorProperty(
    var get: FunctionType? = null,
    var set: FunctionType? = null,
    override var enumerable: Boolean = true,
    override var configurable: Boolean = true,
): Property {
    override fun clone() = copy()

    companion object {
        internal inline fun builtinGetter(name: String, crossinline block: (thisArg: LanguageType) -> NonEmptyNormalOrAbrupt) =
            BuiltinFunctionType(name, 0u) { thisArg, _ ->
                block(thisArg)
            }
        internal inline fun builtinSetter(name: String, crossinline block: (thisArg: LanguageType, value: LanguageType) -> Unit) =
            BuiltinFunctionType(name, 1u) { thisArg, args ->
                block(thisArg, args[0])
                Completion.Normal.`null`
            }
    }
}
