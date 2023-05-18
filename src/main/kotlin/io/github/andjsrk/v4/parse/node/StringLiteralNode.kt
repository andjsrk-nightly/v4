package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.tokenize.Token

class StringLiteralNode(token: Token): DynamicPrimitiveLiteralNode<String>(token), ObjectLiteralKeyNode {
    @EsSpec("SV")
    override val value = token.literal
}
