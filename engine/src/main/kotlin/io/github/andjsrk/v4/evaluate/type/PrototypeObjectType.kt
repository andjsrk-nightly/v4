package io.github.andjsrk.v4.evaluate.type

/**
 * A union of [ClassAssociatedPrototypeObjectType] and [ObjectWrapperPrototypeObjectType]
 */
sealed interface PrototypeObjectType: ObjectType

val PrototypeObjectType.ownerClass get() =
    (this as? ClassAssociatedPrototypeObjectType)
        ?.ownerClass
