package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
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
    override fun evaluate(): MaybeAbrupt<Reference> {
        val baseRef = returnIfAbrupt(`object`.evaluate()) { return it }

        if (baseRef is Reference) {
            val isOptionalChain = baseRef.isOptionalChain || this.isOptionalChain
            if (isOptionalChain && baseRef.base == NullType) return Completion.WideNormal(baseRef) // continue resulting null
        }

        val base = returnIfAbrupt(getValue(baseRef)) { return it }

        if (this.isOptionalChain && base == NullType) return Completion.WideNormal(
            Reference(NullType, null, isOptionalChain=true)
        )

        return Completion.WideNormal(
            if (this.isComputed) {
                val key = property.evaluateValueOrReturn { return it }
                val coercedKey = returnIfAbrupt(key.toPropertyKey()) { return it }
                Reference(base, coercedKey)
            } else {
                require(property is IdentifierNode)
                Reference(base, property.stringValue)
            }
        )
    }
}
