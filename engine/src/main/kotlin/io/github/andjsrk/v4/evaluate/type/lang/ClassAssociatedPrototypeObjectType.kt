package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.Property

/**
 * A prototype object that is associated to a class.
 */
class ClassAssociatedPrototypeObjectType(
    lazyPrototype: Lazy<PrototypeObjectType?> = lazy { null },
    properties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    /**
     * The class that the object is associated to.
     * For example, %Object.prototype%(which is [ClassAssociatedPrototypeObjectType]) is associated to %Object%.
     */
    val ownerClass: ClassType,
): PrototypeObjectType(lazyPrototype, properties)
