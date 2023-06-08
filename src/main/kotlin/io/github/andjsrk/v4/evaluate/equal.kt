package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("IsStrictlyEqual")
fun equal(left: LanguageType, right: LanguageType): BooleanType =
    BooleanType.run {
        if (left::class != right::class) return@run FALSE
        return@run when (left) {
            NullType -> TRUE
            is NumericType<*> -> left.equal(right as NumericType<*>)
            is StringType -> BooleanType.from(left.value == right.value)
            is BooleanType -> BooleanType.from(left.value == right.value)
            is ObjectType -> TODO()
            is SymbolType -> TODO()
        }
    }
