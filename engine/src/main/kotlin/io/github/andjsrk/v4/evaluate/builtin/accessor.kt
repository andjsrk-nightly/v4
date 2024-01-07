package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

internal inline fun PropertyKey.accessor(
    getter: BuiltinFunctionType? = null,
    setter: BuiltinFunctionType? = null,
    enumerable: Boolean? = null,
    configurable: Boolean? = null,
) =
    this to AccessorProperty(getter, setter, enumerable, configurable)

internal inline fun String.accessor(
    getter: BuiltinFunctionType? = null,
    setter: BuiltinFunctionType? = null,
    enumerable: Boolean? = null,
    configurable: Boolean? = null,
) =
    languageValue.accessor(getter, setter, enumerable, configurable)
