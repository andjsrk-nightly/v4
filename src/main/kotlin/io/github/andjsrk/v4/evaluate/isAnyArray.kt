package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.Array
import io.github.andjsrk.v4.evaluate.builtin.MutableArray
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

fun LanguageType.isAnyArray() =
    prototype?.ownerClass?.isSubTypeOfOneOf(Array, MutableArray) ?: false
