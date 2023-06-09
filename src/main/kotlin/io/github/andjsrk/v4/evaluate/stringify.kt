package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

/**
 * Note that the function is not an extension exceptionally due to shadowing of extensions.
 */
@EsSpec("ToString")
internal fun stringify(value: LanguageType): MaybeAbrupt<StringType> {
    return Completion.Normal(
        when (value) {
            NullType -> "null".languageValue
            is BooleanType -> value.value.toString().languageValue
            is StringType -> value
            is NumericType<*> -> value.toString(10)
            // the function allows Symbols to be stringified
            is SymbolType -> value.toString().languageValue
            is ObjectType -> {
                if (value.not { hasProperty(SymbolType.WellKnown.toString) }) return throwError(TypeErrorKind.CANNOT_CONVERT_TO_STRING)
                val toStringMethod = value.getMethod(SymbolType.WellKnown.toString)
                    ?.returnIfAbrupt { return it }
                    ?: return throwError(TypeErrorKind.CANNOT_CONVERT_TO_STRING)
                val string = toStringMethod._call(value, emptyList())
                    .returnIfAbrupt { return it }
                    .requireToBe<StringType> { return it }
                string
            }
        }
    )
}
