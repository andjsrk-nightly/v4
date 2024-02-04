package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.*

sealed class ClassType(
    val name: PropertyKey?,
    val parentPrototype: PrototypeObjectType?,
    staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    open val constructor: FunctionType,
): ObjectType(properties=staticProperties) {
    val instancePrototype: ClassAssociatedPrototypeObjectType =
        ClassAssociatedPrototypeObjectType(lazy { parentPrototype }, instancePrototypeProperties, this)
    init {
        definePropertyOrThrow("instancePrototype".languageValue, DataProperty.sealed(instancePrototype))
    }
    abstract fun construct(args: List<LanguageType>): MaybeAbrupt<ObjectType>
}
