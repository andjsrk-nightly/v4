package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import kotlin.reflect.KClass

fun unexpectedType(actual: LanguageType?, expectedDescription: String) =
    throwError(
        TypeErrorKind.UNEXPECTED_TYPE,
        expectedDescription,
        generalizedDescriptionOf(actual ?: NullType),
    )

fun unexpectedType(actual: LanguageType?, vararg expectedTypes: KClass<out AbstractType>) =
    unexpectedType(
        actual,
        expectedTypes.joinToString(" or ") {
            generalizedDescriptionOf(it)
        }
    )
