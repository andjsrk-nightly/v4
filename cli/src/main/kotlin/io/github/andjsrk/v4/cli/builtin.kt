package io.github.andjsrk.v4.cli

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.sealedMethod
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.normalNull

private val assert = functionWithoutThis("assert", 1u) fn@ { args ->
    val value = args[0].requireToBe<BooleanType> { return@fn it }
    val reason = args.getOptional(1)
        ?.requireToBe<StringType> { return@fn it }
        ?.value
    if (!value.value) {
        if (reason == null) return@fn throwError(RangeErrorKind.ASSERTION_FAILED)
        else return@fn throwError(RangeErrorKind.ASSERTION_FAILED_WITH_REASON, reason)
    }
    normalNull
}

private val log = functionWithoutThis("log", 1u) fn@ { args ->
    println(args.joinToString(" ") { it.display() })
    normalNull
}

internal val globalObj = ObjectType(properties=mutableMapOf(
    sealedMethod(assert),
    sealedMethod(log),
))
