package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.evaluateValue
import io.github.andjsrk.v4.evaluate.type.lang.ImmutableArrayType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArrayLiteralNode(
    override val elements: List<MaybeSpreadNode>,
    override val range: Range,
): CollectionLiteralNode<MaybeSpreadNode> {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate() =
        EvalFlow {
            val values = mutableListOf<LanguageType>()
            for (element in elements) {
                when (element) {
                    is NonSpreadNode -> {
                        val value = element.expression.evaluateValue()
                            .returnIfAbrupt(this) { return@EvalFlow }
                        values += value
                    }
                    is SpreadNode -> {
                        val obj = element.expression.evaluateValue()
                            .returnIfAbrupt(this) { return@EvalFlow }
                        TODO()
                    }
                }
            }
            `return`(
                ImmutableArrayType.from(values)
                    .toNormal()
            )
        }
}
