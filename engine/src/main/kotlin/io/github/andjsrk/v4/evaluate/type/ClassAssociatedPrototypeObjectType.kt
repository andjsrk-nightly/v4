package io.github.andjsrk.v4.evaluate.type

/**
 * A prototype object that is associated to a class.
 */
class ClassAssociatedPrototypeObjectType(
    /**
     * The class that the object is associated to.
     * For example, %Object.prototype% is associated to %Object%.
     */
    val ownerClass: ClassType,
    properties: MutableMap<PropertyKey, Property> = mutableMapOf(),
): PrototypeObjectType, ObjectType by ObjectType.Impl(lazy { ownerClass.parentInstancePrototype }, properties)
