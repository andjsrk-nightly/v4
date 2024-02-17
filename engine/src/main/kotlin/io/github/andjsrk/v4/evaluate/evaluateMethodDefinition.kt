package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*

fun evaluateMethodDefinition(methodNode: MethodNode, obj: ObjectType) = lazyFlow f@ {
    when (methodNode) {
        is NormalMethodNode -> {
            val method = yieldAll(methodNode.evaluate())
                .orReturn { return@f it }
                .apply {
                    homeObject = obj
                }
            obj.defineMethodProperty(method.name!!, method, true)
        }
        is GetterNode -> {
            val getter = yieldAll(methodNode.evaluate())
                .orReturn { return@f it }
                .apply {
                    homeObject = obj
                }
            val name = getter.name!!
            if (name is PrivateName) PrivateProperty(name, AccessorProperty(get=getter)).toWideNormal()
            else {
                val existingDesc = obj._getOwnProperty(name)
                    .orReturnThrow { return@f it }
                obj.properties[name] = when (existingDesc) {
                    null, is DataProperty -> AccessorProperty(get=getter)
                    is AccessorProperty -> existingDesc.apply { get = getter }
                }
                empty
            }
        }
        is SetterNode -> {
            val setter = yieldAll(methodNode.evaluate())
                .orReturn { return@f it }
                .apply {
                    homeObject = obj
                }
            val name = setter.name!!
            if (name is PrivateName) PrivateProperty(name, AccessorProperty(set=setter)).toWideNormal()
            else {
                val existingDesc = obj._getOwnProperty(name)
                    .orReturn { return@f it }
                obj.properties[name] = when (existingDesc) {
                    null, is DataProperty -> AccessorProperty(set = setter)
                    is AccessorProperty -> existingDesc.apply { set = setter }
                }
                empty
            }
        }
        is ConstructorNode -> neverHappens()
    }
}
