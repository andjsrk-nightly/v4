package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.error.*
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.normalNull

private val assert = functionWithoutThis("assert", 1u) fn@ { args ->
    val value = args[0].requireToBe<BooleanType> { return@fn it }
    val reason = args.getOptional(1)
        ?.requireToBeString { return@fn it }
    if (!value.value) {
        if (reason == null) return@fn throwError(RangeErrorKind.ASSERTION_FAILED)
        else return@fn throwError(RangeErrorKind.ASSERTION_FAILED_WITH_REASON, reason)
    }
    normalNull
}

@EsSpec("global object")
val global = ObjectType(properties=mutableMapOf(
    sealedData(::assert),

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
    sealedData(::Reflect),
))
    .apply {
        _defineOwnProperty("global".languageValue, DataProperty.sealed(this))
    }
