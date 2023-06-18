package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

/**
 * Note that the function is not an extension exceptionally due to shadowing of extensions.
 */
@EsSpec("ToString")
internal fun toString(value: LanguageType): Completion {
    return Completion.normal(
        when (value) {
            NullType -> "null".languageValue
            is BooleanType -> value.value.toString().languageValue
            is StringType -> value
            is NumericType<*> -> value.toString(10)
            is SymbolType -> return Completion.`throw`(NullType)
            is ObjectType -> TODO()
        }
    )
}
