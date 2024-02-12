package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
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
            obj.defineMethodProperty(method.name!!, method)
        }
        is GetterNode -> {
            val getter = yieldAll(methodNode.evaluate())
                .orReturn { return@f it }
                .apply {
                    homeObject = obj
                }
            val name = getter.name!!
            TODO()
            val existingDesc = obj._getOwnProperty(name)
                .orReturn { return@f it }
            obj.properties[name] = when (existingDesc) {
                null, is DataProperty -> AccessorProperty(get=getter)
                is AccessorProperty -> existingDesc.apply { get = getter }
            }
        }
        is SetterNode -> {
            val setter = yieldAll(methodNode.evaluate())
                .orReturn { return@f it }
                .apply {
                    homeObject = obj
                }
            val name = setter.name!!
            val existingDesc = obj._getOwnProperty(name)
                .orReturn { return@f it }
            obj.properties[name] = when (existingDesc) {
                null, is DataProperty -> AccessorProperty(set=setter)
                is AccessorProperty -> existingDesc.apply { set = setter }
            }
        }
        is ConstructorNode -> neverHappens()
    }
    empty
}
