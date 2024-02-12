package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.*

sealed class ClassType(
    val name: PropertyKey?,
    val parentInstancePrototype: PrototypeObjectType?,
    staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    open val constructor: FunctionType,
): ObjectType(properties=staticProperties) {
    val privateMethods = mutableSetOf<PrivateProperty>()
    val privateFields = mutableSetOf<PrivateProperty>()
    val instancePrototype: ClassAssociatedPrototypeObjectType =
        ClassAssociatedPrototypeObjectType(lazy { parentInstancePrototype }, instancePrototypeProperties, this)
    init {
        definePropertyOrThrow("instancePrototype".languageValue, DataProperty.sealed(instancePrototype))
    }
    abstract fun construct(args: List<LanguageType>, thisArg: LanguageType = ObjectType(instancePrototype)): MaybeThrow<ObjectType>
}
