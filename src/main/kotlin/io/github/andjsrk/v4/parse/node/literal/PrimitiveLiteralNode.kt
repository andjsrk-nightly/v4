package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.tokenize.Token

abstract class PrimitiveLiteralNode(token: Token): LiteralNode {
    val raw = token.rawContent
    override val range = token.range
    // primitive literals do not need unsealed nodes
}