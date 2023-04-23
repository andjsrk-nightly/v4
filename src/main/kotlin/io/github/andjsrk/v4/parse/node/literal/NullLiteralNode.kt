package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class NullLiteralNode(token: Token): PrimitiveLiteralNode(token) {
    override fun toString() =
        stringifyLikeDataClass(::range) // null has fixed content, so ignoring it
}
