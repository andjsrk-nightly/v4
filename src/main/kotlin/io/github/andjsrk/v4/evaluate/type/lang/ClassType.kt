package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Property

class ClassType(
    val parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeInitializer: ObjectType.() -> Unit,
): ObjectType(staticProperties, null) {
    val instancePrototype = ObjectType.create(parent?.prototype).apply(instancePrototypeInitializer)
    fun construct(): Completion {
        TODO()
    }
}
