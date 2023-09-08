package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

@EsSpec("IsLessThan")
fun LanguageType.lessThan(other: LanguageType): MaybeAbrupt<BooleanType> {
    listOf(this, other).forEach {
        if (it !is StringType && it !is NumericType<*>) return unexpectedType(it, StringType::class, NumericType::class)
    }
    if (this::class != other::class) return throwError(TypeErrorKind.LHS_RHS_NOT_SAME_TYPE)
    return when {
        this is NumberType && other is NumberType -> this.lessThan(other)
        this is BigIntType && other is BigIntType -> this.lessThan(other)
        this is StringType && other is StringType -> this.lessThan(other)
        else -> BooleanType.FALSE.toNormal()
    }
}
