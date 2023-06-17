package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.BigIntType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.tokenize.Token
import java.math.BigInteger

class BigintLiteralNode(token: Token): DynamicPrimitiveLiteralNode<BigInteger>(token) {
    @EsSpec("NumericValue")
    override val value by lazy {
        raw.removeSuffix("n").toBigInteger()
    }
    override fun evaluate() =
        Completion.normal(BigIntType(value))
}
