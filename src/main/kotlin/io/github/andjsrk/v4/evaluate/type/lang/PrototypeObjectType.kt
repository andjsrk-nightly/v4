package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.Property

class PrototypeObjectType(
    lazyPrototype: Lazy<PrototypeObjectType?> = lazy { null },
    properties: MutableMap<PropertyKey, Property>,
    val classOwnsPrototype: ClassType,
): ObjectType(lazyPrototype, properties)
