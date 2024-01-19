package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.lazyFlowNoYields
import io.github.andjsrk.v4.evaluate.type.toNormal
import java.math.BigInteger

class BigintLiteralNode(rawContent: String, range: Range): DynamicPrimitiveLiteralNode<BigInteger>(rawContent, range) {
    @EsSpec("NumericValue")
    override val value by lazy {
        raw.removeSuffix("n").toBigInteger()
    }
    override fun evaluate() = lazyFlowNoYields {
        value
            .languageValue
            .toNormal()
    }
}
