package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.and
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NonRestObjectPropertyNode(
    val key: ObjectLiteralKeyNode,
    binding: BindingElementNode,
    default: ExpressionNode?,
): NonRestNode(binding, default) {
    override val childNodes = key and super.childNodes
    override fun toString() =
        stringifyLikeDataClass(::key, ::binding, ::default, ::range)
}
