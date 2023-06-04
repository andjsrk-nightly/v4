package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.neverHappens

@EsSpec("IsLessThan")
internal fun LanguageType.isLessThan(other: LanguageType, undefinedReplacement: BooleanType): Completion {
    assert(this is NumericType<*> || this is StringType)
    assert(other is NumericType<*> || other is StringType)
    assert(this::class == other::class)
    return Completion.normal(
        when (this) {
            is NumberType -> this.lessThan(other as NumberType, undefinedReplacement)
            is BigIntType -> TODO()
            is StringType -> TODO()
            else -> neverHappens()
        }
    )
}
