package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.*

fun List<MaybeSpreadNode>.toLanguageValueList(): MaybeAbrupt<ListType<LanguageType>> {
    val values = mutableListOf<LanguageType>()
    for (elem in this) {
        val value = elem.expression.evaluateValue()
            .orReturn { return it }
        when (elem) {
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
    return ListType(values.toList())
        .toWideNormal()
}
