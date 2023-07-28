package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.error.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*

private val assert = BuiltinFunctionType("assert", 1u) fn@ { _, args ->
    val value = args[0].requireToBe<BooleanType> { return@fn it }
    val reason = args.getOptional(1)
        ?.requireToBeString { return@fn it }
    if (!value.value) {
        if (reason == null) return@fn throwError(RangeErrorKind.ASSERTION_FAILED)
        else return@fn throwError(RangeErrorKind.ASSERTION_FAILED_WITH_REASON, reason)
    }
    Completion.Normal.`null`
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
    sealedData(::Reflect),
))
    .apply {
        _defineOwnProperty("global".languageValue, DataProperty.sealed(this))
    }
