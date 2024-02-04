package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

/**
 * Indicates a lambda that returns an abrupt completion, which will be used on inline functions.
 */
typealias AbruptReturnLambda = (Completion.Abrupt) -> Nothing
typealias ThrowReturnLambda = (Completion.Throw) -> Nothing

// ? OperationName()
/**
 * Runs [rtn] if the completion is [Completion.Abrupt], returns [Completion.value] otherwise.
 */
@EsSpec("ReturnIfAbrupt")
inline fun <V: AbstractType?> MaybeAbrupt<V>.orReturn(rtn: AbruptReturnLambda) =
    when (this) {
        is Completion.WideNormal -> value
        is Completion.Abrupt -> rtn(this)
    }

/**
 * @see orReturn
 * @see Completion.NonEmptyAbrupt
 */
inline fun <V: AbstractType?> Completion.FromFunctionBody<V>.orReturnNonEmpty(rtn: (Completion.NonEmptyAbrupt) -> Nothing) =
    when (this) {
        is Completion.WideNormal -> value
        is Completion.NonEmptyAbrupt -> rtn(this)
    }

/**
 * @see orReturn
 * @see Completion.Throw
 */
inline fun <V: AbstractType?> MaybeThrow<V>.orReturnThrow(rtn: ThrowReturnLambda) =
    when (this) {
        is Completion.WideNormal -> value
        is Completion.Throw -> rtn(this)
    }

// ! OperationName()
/**
 * Returns [Completion.Normal.value] assuming the completion is a normal completion.
 */
fun <V: AbstractType?> MaybeAbrupt<V>.unwrap() =
    this.orReturn { neverHappens() }
