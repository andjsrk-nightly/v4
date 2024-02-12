package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

inline fun <reified T: LanguageType> LanguageType?.requireToBe(rtn: ThrowReturnLambda): T {
    if (this !is T) rtn(unexpectedType(this, T::class))
    return this
}

// type parameter is for providing proper error message
internal inline fun <reified T: ObjectType> LanguageType?.requireToBe(clazz: ClassType, rtn: ThrowReturnLambda): ObjectType {
    if (this !is ObjectType || this.not { isInstanceOf(clazz) }) rtn(unexpectedType(this, T::class))
    return this
}

internal inline fun LanguageType.requireToBeString(rtn: ThrowReturnLambda) =
    requireToBe<StringType>(rtn)
        .value

internal inline fun LanguageType.requireToBeLanguageTypePropertyKey(rtn: ThrowReturnLambda) =
    if (this !is LanguageTypePropertyKey) rtn(unexpectedType(this, LanguageTypePropertyKey::class))
    else this
