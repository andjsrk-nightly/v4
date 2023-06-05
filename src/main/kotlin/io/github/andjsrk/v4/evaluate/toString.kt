package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion

/**
 * Note that the function is not an extension exceptionally due to shadowing of extensions.
 */
internal fun toString(value: LanguageType): Completion {
    return Completion.normal(
        when (value) {
            is NullType -> StringType("null")
            is BooleanType -> StringType(value.value.toString())
            is StringType -> value
            is NumberType -> value.toString(10u)
            // TODO: BigInt
            is SymbolType -> return Completion(Completion.Type.THROW, NullType)
            // TODO: object
            else -> TODO()
        }
    )
}