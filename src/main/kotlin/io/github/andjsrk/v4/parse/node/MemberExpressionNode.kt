package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Reference
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
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
    override fun evaluate(): Completion {
        val baseRef = returnIfAbrupt(`object`.evaluate()) { return it }

        if (baseRef is Reference) {
            val isOptionalChain = baseRef.isOptionalChain || this.isOptionalChain
            if (isOptionalChain && baseRef.base == NullType) return Completion.wideNormal(baseRef) // continue resulting null
        }

        val base = getLanguageTypeOrReturn(getValue(baseRef)) { return it }

        if (this.isOptionalChain && base == NullType) return Completion.wideNormal(
            Reference(NullType, null, isOptionalChain=true)
        )

        return Completion.wideNormal(
            if (this.isComputed) {
                val key = property.evaluateValueOrReturn { return it }
                if (key !is PropertyKey) return Completion.`throw`(NullType/* TypeError */)
                Reference(base, key)
            } else {
                require(property is IdentifierNode)
                Reference(base, property.stringValue)
            }
        )
    }
}
