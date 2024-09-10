package io.github.andjsrk.v4.evaluate.type

/**
 * A wrapper to an object that is not associated to a class and is used as a prototype object directly.
 */
class ObjectWrapperPrototypeObjectType(val wrapTarget: ObjectType): PrototypeObjectType, ObjectType by wrapTarget
