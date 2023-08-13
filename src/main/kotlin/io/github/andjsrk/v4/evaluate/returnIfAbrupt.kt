package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

/**
 * Indicates a lambda that returns an abrupt completion, which will be used on inline functions.
 */
internal typealias AbruptReturnLambda = (Completion.Abrupt) -> Nothing

// ? OperationName()
/**
 * Runs [rtn] if the completion is [Completion.Abrupt], returns [Completion.value] otherwise.
 */
@EsSpec("ReturnIfAbrupt")
internal inline fun <V: AbstractType?> MaybeAbrupt<V>.orReturn(rtn: AbruptReturnLambda) =
    when (this) {
        is Completion.WideNormal<V> -> value
        is Completion.Abrupt -> rtn(this)
    }

// ! OperationName()
/**
 * Returns [Completion.Normal.value] assuming the completion is a normal completion.
 */
internal fun <V: AbstractType?> MaybeAbrupt<V>.unwrap() =
    this.orReturn { neverHappens() }
