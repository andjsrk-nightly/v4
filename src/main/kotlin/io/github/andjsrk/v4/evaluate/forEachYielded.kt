package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt

/**
 * Performs the given [operation] on values that is yielded(which means elements except return value which is the last element) from a generator.
 */
inline fun Sequence<NonEmptyNormalOrAbrupt>.forEachYielded(operation: (NonEmptyNormalOrAbrupt) -> Unit) {
    val iter = iterator()
    iter.forEach {
        if (iter.hasNext()) operation(it)
    }
}
