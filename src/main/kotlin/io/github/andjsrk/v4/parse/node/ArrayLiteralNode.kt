package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.ArrayType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArrayLiteralNode(
    override val elements: List<MaybeSpreadNode>,
    override val range: Range,
): CollectionLiteralNode<MaybeSpreadNode> {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val values = mutableListOf<LanguageType>()
        for (element in elements) {
            when (element) {
                is NonSpreadNode -> {
                    val value = element.expression.evaluateValueOrReturn { return it }
                    values += value
                }
                is SpreadNode -> {
                    val obj = element.expression.evaluateValueOrReturn { return it }
                    TODO()
                }
            }
        }
        return Completion.Normal(ArrayType.from(values))
    }
}
