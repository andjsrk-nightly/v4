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

fun List<MaybeSpreadNode>.toLanguageValueList() = lazyFlow f@ {
    val values = flatMap { elem ->
        val value = yieldAll(elem.expression.evaluateValue())
            .orReturn { return@f it }
        when (elem) {
            is NonSpreadNode -> listOf(value)
            is SpreadNode ->
                IteratorRecord.from(value)
                    .orReturn { return@f it }
                    .toSequence()
                    .iterator()
                    .toLanguageValueList()
                    .orReturn { return@f it }
        }
    }
    ListType(values)
        .toWideNormal()
}
