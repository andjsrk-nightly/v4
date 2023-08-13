package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.*

@EsSpec("ArgumentListEvaluation") // for Arguments
internal fun evaluateArguments(args: ArgumentsNode): MaybeAbrupt<ListType<LanguageType>> {
    val values = mutableListOf<LanguageType>()
    for (arg in args.elements) {
        when (arg) {
            is NonSpreadNode -> {
                val value = arg.expression.evaluateValue().orReturn { return it }
                values += value
            }
            is SpreadNode -> {
                TODO()
            }
        }
    }
    return ListType(values.toList())
        .toWideNormal()
}
