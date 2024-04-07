package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*

/**
 * Note that static property named `instancePrototype` will be overwritten with its own [instancePrototype].
 */
class BuiltinClassType(
    name: PropertyKey?,
    parentInstancePrototype: PrototypeObjectType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    private val createBasicInstance_: (self: BuiltinClassType) -> ObjectType?,
    override val constructor: BuiltinFunctionType,
): ClassType(name, parentInstancePrototype, staticProperties, instancePrototypeProperties, constructor) {
    constructor(
        name: String,
        parent: ClassType? = null,
        staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
        instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
        createBasicInstance_: (self: BuiltinClassType) -> ObjectType?,
        constructor: BuiltinFunctionType,
    ): this(name.languageValue, parent, staticProperties, instancePrototypeProperties, createBasicInstance_, constructor)
    constructor(
        name: String,
        parentInstancePrototype: PrototypeObjectType? = null,
        staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
        instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
        createBasicInstance_: (self: BuiltinClassType) -> ObjectType?,
        constructor: BuiltinFunctionType,
    ): this(name.languageValue, parentInstancePrototype, staticProperties, instancePrototypeProperties, createBasicInstance_, constructor)
    @EsSpec("BuiltinCallOrConstruct")
    override fun construct(thisArg: LanguageType, args: List<LanguageType>): MaybeThrow<Nothing?> {
        thisArg.requireToBe<ObjectType> { return it }
        withTemporalCtx(constructor.createContextForCall()) {
            constructor.call(thisArg, args)
                .orReturnThrow { return it }
        }
        return empty
    }
    override fun createNearestBuiltinClassInstance() =
        createBasicInstance_(this) ?: parentInstancePrototype?.ownerClass?.createNearestBuiltinClassInstance()
}

internal inline fun constructor(
    requiredParamCount: UInt = 0u,
    crossinline block: (obj: ObjectType, args: List<LanguageType>) -> EmptyOrThrow,
) =
    BuiltinFunctionType("constructor", requiredParamCount) fn@ { obj, args ->
        require(obj is ObjectType)
        block(obj, args)
            .orReturnThrow { return@fn it }
        normalNull
    }
