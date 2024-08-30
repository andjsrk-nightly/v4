package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
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
    override fun evaluate() = lazyFlow f@ {
        val baseRef = yieldAll(`object`.evaluate())
            .orReturn { return@f it }

        if (baseRef is Reference) {
            val isOptionalChain = baseRef.isOptionalChain || isOptionalChain
            if (isOptionalChain && baseRef.base == NullType) return@f baseRef.toWideNormal() // continue resulting null
        }

        val base = getValue(baseRef)
            .orReturn { return@f it }

        if (isOptionalChain && base == NullType) {
            return@f Reference(NullType, null, isOptionalChain = true)
                .toWideNormal()
        }

        if (isComputed) {
            val key = yieldAll(property.evaluateValue())
                .orReturn { return@f it }
                .requireToBeLanguageTypePropertyKey { return@f it }
            Reference(base, key)
        } else {
            require(property is IdentifierNode)
            if (property.isPrivateName) Reference.private(base, property.value)
            else Reference(base, property.stringValue)
        }
                .toWideNormal()
    }
}
