package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

/**
 * Note that the function is not an extension exceptionally due to shadowing of extensions.
 */
@EsSpec("ToString")
internal fun stringify(value: LanguageType): MaybeAbrupt<StringType> {
    return when (value) {
        NullType -> "null".languageValue
        is BooleanType -> value.value.toString().languageValue
        is StringType -> value
        is NumericType<*> -> value.toString(10)
        // the function allows symbols to be stringified
        is SymbolType -> value.toString().languageValue
        is ObjectType -> {
            val toStringMethod = value.getMethod(SymbolType.WellKnown.toString)
                .orReturn { return it }
                ?: return throwError(TypeErrorKind.CANNOT_CONVERT_TO_STRING)
            val string = toStringMethod.call(value, emptyList())
                .orReturn { return it }
                .requireToBe<StringType> { return it }
            string
        }
    }
        .toNormal()
}
