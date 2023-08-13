package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind

/**
 * Handles operations that might be failed with [ConcurrentModificationException].
 */
internal inline fun withUnsafeModification(rtn: AbruptReturnLambda, block: () -> Unit) =
    try {
        block()
    } catch (e: ConcurrentModificationException) {
        rtn(throwError(TypeErrorKind.COLLECTION_MUTATED_WHILE_ITERATION))
    }
