package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.toNormal

class IteratorResult(val source: ObjectType) {
    @EsSpec("IteratorComplete")
    fun getDone(): MaybeAbrupt<BooleanType> {
        return source.get("done".languageValue)
            .orReturn { return it }
            .requireToBe<BooleanType> { return it }
            .toNormal()
    }
    @EsSpec("IteratorValue")
    fun getValue() =
        source.get("value".languageValue)
}

fun ObjectType.asIteratorResult() =
    IteratorResult(this)
