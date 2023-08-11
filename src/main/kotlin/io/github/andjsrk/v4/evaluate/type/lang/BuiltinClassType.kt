package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.type.*

/**
 * Note that static property named `instancePrototype` will be overwritten with its own [instancePrototype].
 */
class BuiltinClassType(
    name: PropertyKey?,
    parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    override val constructor: BuiltinFunctionType,
): ClassType(name, parent, staticProperties, instancePrototypeProperties, constructor) {
    constructor(
        name: String,
        parent: ClassType? = null,
        staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
        instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
        constructor: BuiltinFunctionType,
    ): this(name.languageValue, parent, staticProperties, instancePrototypeProperties, constructor)
    override fun construct(args: List<LanguageType>): MaybeAbrupt<ObjectType> {
        val res = constructor._call(ObjectType.create(instancePrototype), args)
            .returnIfAbrupt { return it }
        require(res is ObjectType)
        return res.toNormal()
    }

    companion object {
        internal inline fun constructor(
            requiredParamCount: UInt = 0u,
            crossinline block: (obj: ObjectType, args: List<LanguageType>) -> MaybeAbrupt<ObjectType>,
        ) =
            BuiltinFunctionType("constructor", requiredParamCount) { obj, args ->
                require(obj is ObjectType)
                block(obj, args)
            }
    }
}
