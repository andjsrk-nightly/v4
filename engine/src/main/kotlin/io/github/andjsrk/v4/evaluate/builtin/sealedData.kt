package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*
import kotlin.reflect.KProperty0

internal inline fun PropertyKey.sealedData(value: LanguageType) =
    this to DataProperty.sealed(value)

internal inline fun String.sealedData(value: LanguageType) =
    languageValue.sealedData(value)

internal fun sealedMethod(method: FunctionType) =
    method.name!!.sealedData(method)

internal fun sealedData(property: KProperty0<LanguageType>) =
    property.name.sealedData(property.get())
