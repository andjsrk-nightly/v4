package io.github.andjsrk.v4.evaluate.builtin.bigint

import io.github.andjsrk.v4.evaluate.getOptional
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val toString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    val radix = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    Completion.Normal(
        bigint.toString(radix)
    )
}
