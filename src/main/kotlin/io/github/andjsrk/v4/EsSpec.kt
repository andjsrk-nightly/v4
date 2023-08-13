package io.github.andjsrk.v4

/**
 * Specifies the target represents something in ES specification.
 *
 * @param specName
 * Represents searchable actual name of the target that is in specification.
 * If it is not searchable, the value should be `"-"`(because annotation parameter cannot be nullable)
 * and KDoc that describes what the target indicating is in specification is needed.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class EsSpec(val specName: String)
