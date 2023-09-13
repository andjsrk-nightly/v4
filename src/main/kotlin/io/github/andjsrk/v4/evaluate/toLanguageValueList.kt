package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.*

fun Iterator<NonEmptyNormalOrAbrupt>.toLanguageValueList(includeReturnValue: Boolean = false): MaybeAbrupt<ListType<LanguageType>> {
    val values = mutableListOf<LanguageType>()
    while (hasNext()) {
        val value = next()
            .orReturn { return it }
        if (hasNext() || includeReturnValue) values += value
    }
    return ListType(values)
        .toWideNormal()
}

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
                    .forEachYielded {
                        values += it.orReturn { return it }
                    }
        }
    }
    return ListType(values)
        .toWideNormal()
}
