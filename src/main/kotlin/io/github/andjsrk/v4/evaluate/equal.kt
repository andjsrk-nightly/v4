package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.FALSE
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.TRUE

@EsSpec("IsStrictlyEqual")
fun equal(left: LanguageType, right: LanguageType): BooleanType =
    if (left::class != right::class) FALSE
    else when (left) {
        NullType -> TRUE
        is NumericType<*> -> left.equal(right as NumericType<*>)
        is StringType -> BooleanType.from(left.value == (right as StringType).value)
        is BooleanType -> BooleanType.from(left.value == (right as BooleanType).value)
        is ObjectType, is SymbolType -> BooleanType.from(left === right)
    }
