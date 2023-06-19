package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Property

class ClassType(
    val parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeInitializer: PrototypeObject.() -> Unit,
): ObjectType(null, staticProperties) {
    val instancePrototype: PrototypeObject = ObjectType.create(parent?.instancePrototype).apply(instancePrototypeInitializer)
    fun construct(): Completion {
        TODO()
    }
}
