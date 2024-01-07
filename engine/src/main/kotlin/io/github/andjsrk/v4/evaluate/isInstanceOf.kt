package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*

private fun PrototypeObjectType.isInPrototypeChainOf(obj: ObjectType): Boolean =
    when (val proto = obj.prototype) {
        this -> true
        null -> false
        else -> this.isInPrototypeChainOf(proto)
    }
fun ObjectType.isInstanceOf(clazz: ClassType) =
    clazz.instancePrototype.isInPrototypeChainOf(this)
