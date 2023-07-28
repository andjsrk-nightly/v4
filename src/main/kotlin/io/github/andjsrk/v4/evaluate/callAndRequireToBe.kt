package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal inline fun <reified R: LanguageType> FunctionType.callAndRequireToBe(thisArg: LanguageType?, args: List<LanguageType>, `return`: AbruptReturnLambda) =
    _call(thisArg, args)
        .returnIfAbrupt(`return`)
        .requireToBe<R>(`return`)
