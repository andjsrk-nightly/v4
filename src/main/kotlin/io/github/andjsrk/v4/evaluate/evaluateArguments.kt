package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.ListType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.toWideNormal
import io.github.andjsrk.v4.parse.node.*

@EsSpec("ArgumentListEvaluation") // for Arguments
internal fun evaluateArguments(args: ArgumentsNode) =
    EvalFlow {
        val values = mutableListOf<LanguageType>()
        for (arg in args.elements) {
            when (arg) {
                is NonSpreadNode -> {
                    val value = arg.expression.evaluateValue()
                        .returnIfAbrupt(this) { return@EvalFlow }
                    values += value
                }
                is SpreadNode -> {
                    TODO()
                }
            }
        }
        `return`(
            ListType(values.toList())
                .toWideNormal()
        )
    }
