package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class IdentifierNode(token: Token): ExpressionNode {
    val value = token.rawContent
    override val range = token.range
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
}
