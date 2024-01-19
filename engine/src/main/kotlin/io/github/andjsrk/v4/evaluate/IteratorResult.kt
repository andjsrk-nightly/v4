package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

class IteratorResult(val source: ObjectType): AbstractType {
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
