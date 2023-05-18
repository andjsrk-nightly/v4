package io.github.andjsrk.v4.parse

/**
 * Specifies the target does not report an error even the parsing was not successful.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
internal annotation class Careful(
    /**
     * Whether the target does not report an error include indirect reports.
     */
    val completely: Boolean = true,
)
