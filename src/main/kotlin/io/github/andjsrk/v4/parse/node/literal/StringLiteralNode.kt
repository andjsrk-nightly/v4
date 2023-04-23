package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.tokenize.Token

class StringLiteralNode(token: Token): DynamicPrimitiveLiteralNode<String>(token) {
    override val value = token.literal
}
