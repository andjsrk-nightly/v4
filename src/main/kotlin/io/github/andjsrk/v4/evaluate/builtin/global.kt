package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.bigint.BigInt
import io.github.andjsrk.v4.evaluate.builtin.error.*
import io.github.andjsrk.v4.evaluate.builtin.function.Function
import io.github.andjsrk.v4.evaluate.builtin.number.Number
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.reflect.Reflect
import io.github.andjsrk.v4.evaluate.builtin.string.String
import io.github.andjsrk.v4.evaluate.builtin.symbol.Symbol
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("global object")
val global = ObjectType(properties=mutableMapOf(
    // TODO: function properties

    // 19.3 (Constructor Properties)
    sealedData(::BigInt),
    sealedData(::Error),
    sealedData(::Function),
    sealedData(::Number),
    sealedData(::Object),
    sealedData(::RangeError),
    sealedData(::ReferenceError),
    sealedData(::String),
    sealedData(::Symbol),
    sealedData(::SyntaxError),
    sealedData(::TypeError),

    // 19.4 (Other Properties)
    sealedData(::Reflect),
))
    .apply {
        set("global".languageValue, this)
    }
