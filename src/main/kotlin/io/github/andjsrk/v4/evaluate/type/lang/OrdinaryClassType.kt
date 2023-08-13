package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.*

class OrdinaryClassType(
    name: PropertyKey?,
    parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    override val constructor: OrdinaryFunctionType,
): ClassType(name, parent, staticProperties, instancePrototypeProperties, constructor) {
    override fun construct(args: List<LanguageType>): MaybeAbrupt<ObjectType> {
        val obj = ObjectType(instancePrototype)
        TODO()
        return obj.toNormal()
    }
}
