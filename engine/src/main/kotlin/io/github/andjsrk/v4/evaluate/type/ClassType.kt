package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.languageValue

sealed class ClassType(
    val name: PropertyKey?,
    val parentInstancePrototype: PrototypeObjectType?,
    staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    open val constructor: FunctionType,
): ObjectType(properties=staticProperties) {
    val privateInstanceMethods = mutableMapOf<PrivateName, PrivateProperty>()
    val instanceFields = mutableMapOf<PropertyKey, ClassFieldDefinition>()
    val instancePrototype: ClassAssociatedPrototypeObjectType =
        ClassAssociatedPrototypeObjectType(lazy { parentInstancePrototype }, instancePrototypeProperties, this)
    init {
        definePropertyOrThrow("instancePrototype".languageValue, DataProperty.sealed(instancePrototype))
    }
    abstract fun construct(args: List<LanguageType>, thisArg: LanguageType = ObjectType(instancePrototype)): MaybeThrow<ObjectType>
}
