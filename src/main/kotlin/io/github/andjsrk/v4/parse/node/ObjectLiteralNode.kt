package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.*

class ObjectLiteralNode(
    override val elements: List<ObjectElementNode>,
    override val range: Range,
): CollectionLiteralNode<ObjectElementNode> {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate() =
        EvalFlow {
            val obj = ObjectType.createNormal()
            for (element in elements) {
                 evaluatePropertyDefinition(obj, element)
                    .returnIfAbrupt(this) { return@EvalFlow }
            }
            `return`(obj.toNormal())
        }
    private fun evaluatePropertyDefinition(obj: ObjectType, property: ObjectElementNode) =
        EvalFlow {
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
                                val value = expression.evaluateValue()
                                    .returnIfAbrupt(this@EvalFlow) { return@EvalFlow }
                                value.toPropertyKey()
                                    .returnIfAbrupt { return@EvalFlow }
                            }
                            is IdentifierNode -> stringValue
                            is NumberLiteralNode -> value.languageValue.toString(10)
                            is StringLiteralNode -> value.languageValue
                        }
                    }
                    val value = when {
                        isShorthand && keyNode is ComputedPropertyKeyNode -> key // should be evaluated only once in this case
                        property.value.isAnonymous -> property.value.evaluateWithName(key)
                        else ->
                            property.value.evaluateValue()
                                .returnIfAbrupt(this) { return@EvalFlow }
                    }
                    obj.createDataPropertyOrThrow(key, value)
                }
                is SpreadNode -> {
                    val value = property.expression.evaluateValue()
                        .returnIfAbrupt(this) { return@EvalFlow }
                    TODO()
                }
                else -> TODO()
            }
        }
}
