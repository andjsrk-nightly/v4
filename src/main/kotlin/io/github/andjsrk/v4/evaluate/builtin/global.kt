package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.number.Number
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.reflect.Reflect
import io.github.andjsrk.v4.evaluate.builtin.string.String
import io.github.andjsrk.v4.evaluate.builtin.symbol.Symbol
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("global object")
val global = ObjectType.createBasic().apply {
    set("global".languageValue, this)
    // TODO: function properties
    set("Number".languageValue, Number)
    set("Object".languageValue, Object)
    set("String".languageValue, String)
    set("Symbol".languageValue, Symbol)
    set("Reflect".languageValue, Reflect)
}
