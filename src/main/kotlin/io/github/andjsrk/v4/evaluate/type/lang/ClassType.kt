package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.*

sealed class ClassType(
    val name: PropertyKey?,
    val parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    open val constructor: FunctionType,
): ObjectType(lazy { Object.instancePrototype }, staticProperties) {
    val instancePrototype: PrototypeObjectType =
        PrototypeObjectType(lazy { parent?.instancePrototype }, instancePrototypeProperties, this)
    init {
        definePropertyOrThrow("instancePrototype".languageValue, DataProperty.sealed(instancePrototype))
    }
    abstract fun construct(args: List<LanguageType>): MaybeAbrupt<ObjectType>
}
