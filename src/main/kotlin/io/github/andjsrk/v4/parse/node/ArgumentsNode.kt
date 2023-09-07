package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValue
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArgumentsNode(
    val elements: List<MaybeSpreadNode>,
    override val range: Range,
): NonAtomicNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    @EsSpec("ArgumentListEvaluation") // for Arguments
    override fun evaluate(): MaybeAbrupt<ListType<LanguageType>> {
        val values = mutableListOf<LanguageType>()
        for (arg in elements) {
            when (arg) {
                is NonSpreadNode -> {
                    val value = arg.expression.evaluateValue()
                        .orReturn { return it }
                    values += value
                }
                is SpreadNode -> {
                    TODO()
                }
            }
        }
        return Completion.WideNormal(
            ListType(values)
        )
    }
}
