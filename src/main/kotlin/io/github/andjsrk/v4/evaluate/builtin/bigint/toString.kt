package io.github.andjsrk.v4.evaluate.builtin.bigint

import io.github.andjsrk.v4.evaluate.normalizeNull
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val toString = BuiltinFunctionType(SymbolType.WellKnown.toString) fn@ { thisArg, args ->
    val bigint = thisArg
        .requireToBe<BigIntType> { return@fn it }
    val radix = args.getOrNull(0)
        ?.normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    Completion.Normal(
        bigint.toString(radix)
    )
}
