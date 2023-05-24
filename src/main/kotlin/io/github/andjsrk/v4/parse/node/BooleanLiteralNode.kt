package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.tokenize.Token

class BooleanLiteralNode(token: Token): DynamicPrimitiveLiteralNode<Boolean>(token) {
    override val value = raw.toBooleanStrict()
}