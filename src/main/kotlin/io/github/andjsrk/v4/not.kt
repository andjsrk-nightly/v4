package io.github.andjsrk.v4

/**
 * With this function, `a.b.not()` can be replaced with `a.not { b }`,
 * which has similar word order to `a !in b`, `a !is B`.
 */
internal inline fun <T> T.not(block: T.() -> Boolean) =
    !block()
