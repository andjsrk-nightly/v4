package io.github.andjsrk.v4.parse

/**
 * Specifies whether the target does not report an error even the starting token is not expected one.
 */
@Target(AnnotationTarget.FUNCTION)
internal annotation class Careful(
    val value: Boolean = true,
)
