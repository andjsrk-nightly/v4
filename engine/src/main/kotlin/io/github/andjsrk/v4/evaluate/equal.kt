package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("IsStrictlyEqual")
fun equal(left: LanguageType, right: LanguageType): Boolean =
    if (left::class != right::class) false
    else when (left) {
        NullType -> true
        is NumericType<*> -> left.equal(right as NumericType<*>)
        is StringType -> left.value == (right as StringType).value
        is BooleanType -> left.value == (right as BooleanType).value
        is ObjectType, is SymbolType -> left === right
    }
