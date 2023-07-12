package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import kotlin.reflect.KClass

internal fun unexpectedType(actual: LanguageType?, expectedDescription: String) =
    throwError(
        TypeErrorKind.UNEXPECTED_TYPE,
        expectedDescription,
        generalizedDescriptionOf(actual ?: NullType),
    )

internal fun unexpectedType(actual: LanguageType?, vararg expectedTypes: KClass<out LanguageType>) =
    unexpectedType(
        actual,
        expectedTypes.joinToString(" or ") {
            generalizedDescriptionOf(it)
        }
    )
