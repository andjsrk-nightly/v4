package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.*

class OrdinaryClassType(
    name: PropertyKey?,
    parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    override val constructor: OrdinaryFunctionType,
): ClassType(name, parent?.instancePrototype, staticProperties, instancePrototypeProperties, constructor) {
    override fun construct(args: List<LanguageType>, thisArg: LanguageType): MaybeThrow<ObjectType> {
        val obj = thisArg
            .requireToBe<ObjectType> { return it }
        TODO()
        return obj.toNormal()
    }
}
