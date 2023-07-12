package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.parse.*

class ObjectLiteralNode(
    override val elements: List<ObjectElementNode>,
    override val range: Range,
): CollectionLiteralNode<ObjectElementNode> {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val obj = ObjectType.createNormal()
        for (element in elements) {
            evaluatePropertyDefinition(obj, element)
                .returnIfAbrupt { return it }
        }
        return Completion.Normal(obj)
    }
    private fun evaluatePropertyDefinition(obj: ObjectType, property: ObjectElementNode): EmptyOrAbrupt {
        when (property) {
            is PropertyNode -> {
                val keyNode = property.key
                val isShorthand = when (keyNode) {
                    is ComputedPropertyKeyNode -> keyNode.expression.range == property.value.range
                    else -> keyNode.range == property.value.range
                }
                val key: PropertyKey = with (keyNode) {
                    when (this) {
                        is ComputedPropertyKeyNode -> {
                            val value = expression.evaluateValueOrReturn { return it }
                            value.toPropertyKey()
                                .returnIfAbrupt { return it }
                        }
                        is IdentifierNode -> stringValue
                        is NumberLiteralNode -> value.languageValue.toString(10)
                        is StringLiteralNode -> value.languageValue
                    }
                }
                val value = when {
                    isShorthand && keyNode is ComputedPropertyKeyNode -> key // should be evaluated only once in this case
                    property.value.isAnonymous -> property.value.evaluateWithNameOrReturn(key) { return it }
                    else -> property.value.evaluateValueOrReturn { return it }
                }
                obj.createDataPropertyOrThrow(key, value)
                return empty
            }
            is SpreadNode -> {
                val value = property.expression.evaluateValueOrReturn { return it }
                TODO()
            }
            else -> TODO()
        }
    }
}
