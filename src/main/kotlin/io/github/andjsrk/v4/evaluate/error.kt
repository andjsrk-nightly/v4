package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.*
import io.github.andjsrk.v4.evaluate.builtin.error.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

internal fun error(kind: ErrorKind, vararg args: String): ObjectType {
    val errorClass = when (kind) {
        is RangeErrorKind -> RangeError
        is ReferenceErrorKind -> ReferenceError
        is SyntaxErrorKind -> SyntaxError
        is TypeErrorKind -> TypeError
    }
    var i = 0
    val instantiatedErrorMessage = kind.message.replace(Regex("(?<!\\\\)%")) {
        // replace each '%' in message with a string in `args`
        args.getOrNull(i).also { i++ } ?: throw IllegalArgumentException("Arguments less than expected has provided.")
    }
    return error(errorClass, instantiatedErrorMessage)
}

/**
 * Returns an instance of `%Error%`(or its subclasses).
 */
internal fun error(errorClass: BuiltinClassType, message: String): ObjectType {
    assert(errorClass.isNativeError)
    return neverAbrupt(errorClass.construct(listOf(message.languageValue)))
}

private val BuiltinClassType.isNativeError get() =
    this == Error || instancePrototype.prototype?.ownerClass == Error
