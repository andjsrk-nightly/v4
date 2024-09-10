package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectLiteralNode(
    override val elements: List<ObjectElementNode>,
    override val range: Range,
): CollectionLiteralNode<ObjectElementNode> {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate() = lazyFlow f@ {
        val obj = ObjectType.Impl()
        elements.forEach {
            yieldAll(evaluatePropertyDefinition(obj, it))
                .orReturn { return@f it }
        }
        obj.toNormal()
    }
    private fun evaluatePropertyDefinition(obj: ObjectType, property: ObjectElementNode) = lazyFlow f@ {
        when (property) {
            is PropertyNode -> {
                val keyNode = property.key
                val isShorthand = when (keyNode) {
                    is ComputedPropertyKeyNode -> keyNode.expression.range == property.value.range
                    else -> keyNode.range == property.value.range
                }
                val key = yieldAll(keyNode.toLanguageTypePropertyKey())
                    .orReturn { return@f it }
                val value = when {
                    isShorthand && keyNode is ComputedPropertyKeyNode -> key // should be evaluated only once in this case
                    property.value.isAnonymous -> property.value.evaluateWithName(key)
                    else -> yieldAll(property.value.evaluateValue())
                        .orReturn { return@f it }
                }
                obj.createDataProperty(key, value)
            }
            is SpreadNode -> {
                val value = yieldAll(property.expression.evaluateValue())
                    .orReturn { return@f it }
                copyDataProperties(obj, value)
                    .orReturn { return@f it }
            }
            is NonSpreadNode -> neverHappens()
            is MethodNode -> evaluateMethodDefinition(property, obj)
        }
        empty
    }
}
