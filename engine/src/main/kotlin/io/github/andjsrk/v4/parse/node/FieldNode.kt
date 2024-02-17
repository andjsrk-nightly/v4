package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class FieldNode(
    val name: ObjectLiteralKeyNode,
    val value: ExpressionNode?,
    override val isStatic: Boolean,
    startRange: Range,
    semicolonRange: Range?,
): NormalClassElementNode, EvaluationDelegatedNode {
    override val childNodes get() = listOf(name, value)
    override val range = startRange..(value ?: name).range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::name, ::value, ::isStatic, ::range)
}
