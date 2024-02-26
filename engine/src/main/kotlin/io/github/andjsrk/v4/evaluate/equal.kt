package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("IsStrictlyEqual")
fun equal(left: LanguageType, right: LanguageType): Boolean =
    if (left::class != right::class) false
    else when (left) {
        NullType -> true
        is NumericType<*> -> left.equal(right as NumericType<*>)
        is StringType -> left.nativeValue == (right as StringType).nativeValue
        is BooleanType -> left.nativeValue == (right as BooleanType).nativeValue
        is ObjectType, is SymbolType -> left === right
    }
