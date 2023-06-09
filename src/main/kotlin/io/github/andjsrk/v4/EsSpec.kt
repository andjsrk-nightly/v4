package io.github.andjsrk.v4

/**
 * Specifies the target represents something in ES specification.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class EsSpec(
    /**
     * Represents searchable actual name of the target that is in specification.
     * If it is not searchable, the value should be `"-"`(because annotation parameter cannot be nullable).
     */
    val specName: String,
)
