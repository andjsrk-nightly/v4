package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class ThisReferenceNode(token: Token): ExpressionNode {
    override val range = token.range
    override fun toString() =
        stringifyLikeDataClass(::range)
}
