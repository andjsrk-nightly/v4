package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.*

inline fun PropertyKey.accessor(
    getter: BuiltinFunctionType? = null,
    setter: BuiltinFunctionType? = null,
    enumerable: Boolean? = null,
    configurable: Boolean? = null,
) =
    this to AccessorProperty(getter, setter, enumerable, configurable)

inline fun String.accessor(
    getter: BuiltinFunctionType? = null,
    setter: BuiltinFunctionType? = null,
    enumerable: Boolean? = null,
    configurable: Boolean? = null,
) =
    languageValue.accessor(getter, setter, enumerable, configurable)
