package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.Completion

/**
 * Note that the function is not an extension exceptionally due to shadowing of extensions.
 */
@EsSpec("ToString")
internal fun toString(value: LanguageType): Completion {
    return Completion.normal(
        when (value) {
            NullType -> StringType("null")
            is BooleanType -> StringType(value.value.toString())
            is StringType -> value
            is NumericType<*> -> value.toString(10)
            is SymbolType -> return Completion.`throw`(NullType)
            is ObjectType -> TODO()
        }
    )
}
