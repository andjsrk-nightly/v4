package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.*

fun Iterator<NonEmptyNormalOrAbrupt>.toLanguageValueList(includeReturnValue: Boolean = false): MaybeAbrupt<ListType<LanguageType>> {
    val iter =
        if (includeReturnValue) this
        else withoutLast()
    val values = iter
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
                iterableToSequence(value)
                    .orReturn { return it }
                    .value
                    .iterator()
                    .toLanguageValueList()
                    .orReturn { return it }
        }
    }
    return ListType(values)
        .toWideNormal()
}
