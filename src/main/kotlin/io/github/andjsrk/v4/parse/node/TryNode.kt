package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class TryNode(
    val tryBody: BlockNode,
    val catch: CatchNode?,
    val finallyBody: BlockNode?,
    startRange: Range,
): StatementNode, NonAtomicNode {
    override val childNodes get() = listOf(tryBody, catch, finallyBody)
    override val range = startRange..(finallyBody ?: catch?.body)!!.range
    override fun toString() =
        stringifyLikeDataClass(::tryBody, ::catch, ::finallyBody, ::range)
}
