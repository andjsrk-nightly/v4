package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.error.*
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.unwrap

@EsSpec("global object")
val global = ObjectType(properties=mutableMapOf(
    // 19.3 (Constructor Properties)
    sealedData(::Array),
    sealedData(::BigInt),
    sealedData(::Error),
    sealedData(::Function),
    sealedData(::MutableArray),
    sealedData(::Number),
    sealedData(::Object),
    sealedData(::RangeError),
    sealedData(::ReferenceError),
    sealedData(::String),
    sealedData(::Symbol),
    sealedData(::SyntaxError),
    sealedData(::TypeError),

    // 19.4 (Other Properties)
    sealedData(::Json),
    sealedData(::Math),
    sealedData(::Reflect),
))
    .apply {
        _defineOwnProperty("global".languageValue, DataProperty.sealed(this))
            .unwrap()
    }
