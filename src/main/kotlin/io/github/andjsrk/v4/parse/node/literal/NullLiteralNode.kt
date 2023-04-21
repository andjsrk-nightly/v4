package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class NullLiteralNode(token: Token): LiteralNode {
    override val range = token.range
    override fun toString() =
        stringifyLikeDataClass(::range)
}
