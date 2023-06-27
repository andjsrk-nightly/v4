package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.type.*

/**
 * Note that static property named `instancePrototype` will be overwritten with its own [instancePrototype].
 */
class BuiltinClassType(
    parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    constructor: BuiltinFunctionType,
): ClassType(parent, staticProperties, instancePrototypeProperties, constructor) {
    override fun construct(args: List<LanguageType>): MaybeAbrupt<ObjectType> {
        val res = returnIfAbrupt(constructor._call(NullType, args)) { return it }
        require(res is ObjectType)
        return Completion.Normal(res)
    }

    companion object {
        internal inline fun constructor(requiredParameterCount: UInt = 0u, crossinline block: (args: List<LanguageType>) -> MaybeAbrupt<ObjectType>) =
            BuiltinFunctionType("constructor".languageValue, requiredParameterCount) { _, args ->
                block(args)
            }
    }
}
