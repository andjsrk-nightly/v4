package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens

@EsSpec("IsLessThan")
internal fun LanguageType.isLessThan(other: LanguageType): MaybeAbrupt<BooleanType> {
    assert(this is NumericType<*> || this is StringType)
    assert(other is NumericType<*> || other is StringType)
    assert(this::class == other::class)
    return when (this) {
        is NumberType -> this.lessThan(other as NumberType)
        is BigIntType -> this.lessThan(other as BigIntType)
        is StringType -> TODO()
        else -> neverHappens()
    }
}
