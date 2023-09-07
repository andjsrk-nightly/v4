package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

@EsSpec("IsLessThan")
internal fun LanguageType.lessThan(other: LanguageType): MaybeAbrupt<BooleanType> {
    // TODO: return throw completion if this and other have not same type
    return when {
        this is NumberType && other is NumberType -> this.lessThan(other)
        this is BigIntType && other is BigIntType -> this.lessThan(other)
        this is StringType && other is StringType -> this.lessThan(other)
        else -> BooleanType.FALSE.toNormal()
    }
}
