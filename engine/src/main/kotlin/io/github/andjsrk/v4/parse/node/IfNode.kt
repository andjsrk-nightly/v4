package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class IfNode<N: Node>( // used generic parameter to coerce both `then` and `else` to be of same type
    val test: ExpressionNode,
    val then: N,
    open val `else`: N?,
): NonAtomicNode {
    override val childNodes get() = listOf(test, then, `else`)
    override fun toString() =
        stringifyLikeDataClass(::test, ::then, ::`else`, ::range)
}
