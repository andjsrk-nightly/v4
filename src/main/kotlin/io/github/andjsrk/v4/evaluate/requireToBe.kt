package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

internal inline fun <reified T: LanguageType> LanguageType?.requireToBe(rtn: AbruptReturnLambda): T {
    if (this !is T) rtn(unexpectedType(this, T::class))
    return this
}

// type parameter is for providing proper error message
internal inline fun <reified T: ObjectType> LanguageType?.requireToBe(clazz: ClassType, rtn: AbruptReturnLambda): ObjectType {
    if (this !is ObjectType || this.not { isInstanceOfOneOf(clazz) }) rtn(unexpectedType(this, T::class))
    return this
}

internal inline fun LanguageType.requireToBeString(rtn: AbruptReturnLambda) =
    requireToBe<StringType>(rtn)
        .value
