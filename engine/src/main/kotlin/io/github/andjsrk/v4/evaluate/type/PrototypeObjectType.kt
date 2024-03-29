package io.github.andjsrk.v4.evaluate.type

typealias PrototypeObjectType = ObjectType

val PrototypeObjectType.ownerClass get() =
    (this as? ClassAssociatedPrototypeObjectType)
        ?.ownerClass
