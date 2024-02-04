package io.github.andjsrk.v4

/**
 * Specifies a code that is annotated with this is written because of false positive of Kotlin compiler.
 */
@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class CompilerFalsePositive
