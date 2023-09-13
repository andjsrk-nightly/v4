package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectLiteralNode(
    override val elements: List<ObjectElementNode>,
    override val range: Range,
): CollectionLiteralNode<ObjectElementNode> {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val obj = ObjectType.createNormal()
        elements.forEach {
            evaluatePropertyDefinition(obj, it)
                .orReturn { return it }
        }
        return obj.toNormal()
    }
    private fun evaluatePropertyDefinition(obj: ObjectType, property: ObjectElementNode): EmptyOrAbrupt {
        when (property) {
            is PropertyNode -> {
                val keyNode = property.key
                val isShorthand = when (keyNode) {
                    is ComputedPropertyKeyNode -> keyNode.expression.range == property.value.range
                    else -> keyNode.range == property.value.range
                }
                val key = keyNode.toPropertyKey()
                    .orReturn { return it }
                val value = when {
                    isShorthand && keyNode is ComputedPropertyKeyNode -> key // should be evaluated only once in this case
                    property.value.isAnonymous -> property.value.evaluateWithName(key)
                    else -> property.value.evaluateValue()
                        .orReturn { return it }
                }
                obj.createDataProperty(key, value)
            }
            is SpreadNode -> {
                val value = property.expression.evaluateValue()
                    .orReturn { return it }
                copyDataProperties(obj, value)
                    .orReturn { return it }
            }
            else -> TODO()
        }
        return empty
    }
}
