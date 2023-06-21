package io.github.andjsrk.v4

/**
 * Specifies the target represents something in ES specification.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Repeatable
annotation class EsSpec(
    /**
     * Represents searchable actual name of the target that is in specification.
     * If it is not searchable, the value should be `"-"`(because annotation parameter cannot be nullable)
     * and KDoc that describes what the target indicating is in specification is needed.
     */
    val specName: String,
)
