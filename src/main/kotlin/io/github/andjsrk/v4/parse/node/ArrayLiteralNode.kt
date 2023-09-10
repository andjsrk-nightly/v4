package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
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
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val values = mutableListOf<LanguageType>()
        for (element in elements) {
            val value = element.expression.evaluateValue()
                .orReturn { return it }
            when (element) {
                is NonSpreadNode -> values += value
                is SpreadNode ->
                    iterableToSequence(value)
                        .orReturn { return it }
                        .value
                        .forEachYielded { item ->
                            values += item.orReturn { return it }
                        }
            }
        }
        return ImmutableArrayType.from(values)
            .toNormal()
    }
}
