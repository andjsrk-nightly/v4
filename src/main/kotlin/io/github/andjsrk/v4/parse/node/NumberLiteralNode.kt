package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

class NumberLiteralNode(rawContent: String, range: Range): DynamicPrimitiveLiteralNode<Double>(rawContent, range), ObjectLiteralKeyNode {
    @EsSpec("NumericValue")
    override val value by lazy {
        parseNumber(raw)!!
    }
    override fun evaluate() =
        Completion.Normal(NumberType(value))
}
