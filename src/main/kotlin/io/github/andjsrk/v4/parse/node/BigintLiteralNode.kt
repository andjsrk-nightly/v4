package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BigIntType
import java.math.BigInteger

class BigintLiteralNode(rawContent: String, range: Range): DynamicPrimitiveLiteralNode<BigInteger>(rawContent, range) {
    @EsSpec("NumericValue")
    override val value by lazy {
        raw.removeSuffix("n").toBigInteger()
    }
    override fun evaluate() =
        Completion.Normal(BigIntType(value))
}
