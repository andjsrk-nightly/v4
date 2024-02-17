package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

class IteratorResult(val source: ObjectType): AbstractType {
    @EsSpec("IteratorComplete")
    fun getDone(): MaybeThrow<BooleanType> {
        return source.get("done".languageValue)
            .orReturnThrow { return it }
            .requireToBe<BooleanType> { return it }
            .toNormal()
    }
    @EsSpec("IteratorValue")
    fun getValue() =
        source.get("value".languageValue)
}

fun ObjectType.asIteratorResult() =
    IteratorResult(this)
