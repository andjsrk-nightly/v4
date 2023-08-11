package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.singleNormalEvaluationResult

class NumberLiteralNode(rawContent: String, range: Range): DynamicPrimitiveLiteralNode<Double>(rawContent, range), ObjectLiteralKeyNode {
    @EsSpec("NumericValue")
    override val value by lazy {
        parseNumber(raw).value
    }
    override fun evaluate() =
        singleNormalEvaluationResult(
            value.languageValue
        )
}
