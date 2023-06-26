package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.Property

sealed class ClassType(
    val parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    val constructor: FunctionType,
): ObjectType(lazy { null }, staticProperties) {
    val instancePrototype: PrototypeObject =
        ObjectType(lazy { parent?.instancePrototype }, instancePrototypeProperties)
    abstract fun construct(args: List<LanguageType>): MaybeAbrupt<ObjectType>
}
