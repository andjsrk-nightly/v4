package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.tokenize.Token
import java.math.BigInteger

class BigintLiteralNode(token: Token): DynamicPrimitiveLiteralNode<BigInteger>(token) {
    override val value by lazy {
        raw.toBigInteger()
    }
}
