package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.HostConfig
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.error.*
import io.github.andjsrk.v4.evaluate.type.*

val wait = functionWithoutThis("wait", 1u) fn@ { args ->
    val ms = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeUnsignedInt { return@fn it }
    HostConfig.value.wait(ms)
        .toNormal()
}

@EsSpec("global object")
val global = ObjectType.Impl(mutableMapOf(
    // 19.3 (Constructor Properties)
    sealedData(::Array),
    sealedData(::BigInt),
    sealedData(::Error),
    sealedData(::Function),
    sealedData(::MutableArray),
    sealedData(::Number),
    sealedData(::Object),
    sealedData(::Promise),
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

    sealedMethod(wait),
))
    .apply {
        _defineOwnProperty("global".languageValue, DataProperty.sealed(this))
            .unwrap()
    }
