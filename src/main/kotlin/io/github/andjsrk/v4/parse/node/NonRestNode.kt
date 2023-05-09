package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

typealias IdentifierOrBindingPatternNode = Node

open class NonRestNode(
    override val `as`: IdentifierOrBindingPatternNode,
    val default: ExpressionNode?,
): MaybeRestNode {
    override val range = `as`.range..(default ?: `as`).range
    override fun toString() =
        stringifyLikeDataClass(::`as`, ::default, ::range)
}
