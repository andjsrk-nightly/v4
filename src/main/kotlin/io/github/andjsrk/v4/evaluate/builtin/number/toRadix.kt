package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod
import io.github.andjsrk.v4.evaluate.type.lang.isInteger
import io.github.andjsrk.v4.evaluate.type.lang.requireToBeRadix
import io.github.andjsrk.v4.not

@EsSpec("Number.prototype.toString") // with radix
val toRadix = builtinMethod("toRadix", 1u) fn@ { thisArg, args ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    val radix = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRadix { return@fn it }
    if (radix != 10 && number.isFinite && number.not { isInteger }) return@fn throwError(TypeErrorKind.NON_INTEGER_TO_NON_DECIMAL)
    Completion.Normal(
        number.toString(radix)
    )
}
