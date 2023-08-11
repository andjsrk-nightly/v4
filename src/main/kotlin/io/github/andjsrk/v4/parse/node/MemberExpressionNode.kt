package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Reference
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.stringValue
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class MemberExpressionNode(
    override val `object`: ExpressionNode,
    override val property: ExpressionNode,
    val isOptionalChain: Boolean,
    override val isComputed: Boolean,
    endRange: Range,
): MemberExpressionLikeNode {
    override val childNodes get() = listOf(`object`, property)
    override val range = `object`.range..endRange
    override fun toString() =
        stringifyLikeDataClass(::`object`, ::property, ::isOptionalChain, ::isComputed, ::range)
    override fun evaluate() =
        EvalFlow {
            val baseRef = `object`.evaluate()
                .returnIfAbrupt(this) { return@EvalFlow }

            if (baseRef is Reference) {
                val isOptionalChain = baseRef.isOptionalChain || isOptionalChain
                if (isOptionalChain && baseRef.base == NullType) `return`(Completion.WideNormal(baseRef)) // continue resulting null
            }

            val base = getValue(baseRef)
                .returnIfAbrupt { `return`(it) }

            if (isOptionalChain && base == NullType) `return`(
                Completion.WideNormal(
                    Reference(NullType, null, isOptionalChain=true)
                )
            )

            `return`(
                Completion.WideNormal(
                    if (isComputed) {
                        val key = property.evaluateValue()
                            .returnIfAbrupt(this) { return@EvalFlow }
                        val coercedKey = key.toPropertyKey()
                            .returnIfAbrupt { `return`(it) }
                        Reference(base, coercedKey)
                    } else {
                        require(property is IdentifierNode)
                        Reference(base, property.stringValue)
                    }
                )
            )
        }
}
