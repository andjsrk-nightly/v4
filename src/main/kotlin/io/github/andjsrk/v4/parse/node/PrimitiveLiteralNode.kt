package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.tokenize.Token

sealed class PrimitiveLiteralNode(token: Token): LiteralNode {
    val raw = token.rawContent
    override val range = token.range
}
