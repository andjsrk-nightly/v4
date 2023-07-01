package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.bigint.BigInt
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
    arrayOf(BigInt, Number, Object, String, Symbol).forEach {
        set(it.name!!, it)
    }
    set("Reflect".languageValue, Reflect)
}
