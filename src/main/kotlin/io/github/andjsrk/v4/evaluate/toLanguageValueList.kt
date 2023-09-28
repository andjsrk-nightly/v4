package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.*

fun Iterator<NonEmptyOrAbrupt>.toLanguageValueList(): MaybeAbrupt<ListType<LanguageType>> {
    val values = this
        .asSequence()
        .toList()
        .map {
            it.orReturn { return it }
        }
    return ListType(values)
        .toWideNormal()
}

fun List<MaybeSpreadNode>.toLanguageValueList(): MaybeAbrupt<ListType<LanguageType>> {
    val values = flatMap { elem ->
        val value = elem.expression.evaluateValue()
            .orReturn { return it }
        when (elem) {
            is NonSpreadNode -> listOf(value)
            is SpreadNode ->
                IteratorRecord.from(value)
                    .orReturn { return it }
                    .toSequence()
                    .iterator()
                    .toLanguageValueList()
                    .orReturn { return it }
        }
    }
    return ListType(values)
        .toWideNormal()
}
