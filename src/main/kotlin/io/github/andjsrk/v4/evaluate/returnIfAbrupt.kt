package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

/**
 * Indicates a lambda that returns an abrupt completion, which will be used on inline functions.
 */
internal typealias AbruptReturnLambda = (Completion.Abrupt) -> Nothing

@EsSpec("ReturnIfAbrupt")
inline fun <V: AbstractType?> MaybeAbrupt<V>.returnIfAbrupt(rtn: AbruptReturnLambda) =
    when (this) {
        is Completion.Abrupt -> rtn(this)
        is Completion.WideNormal -> value
    }

inline fun <V: AbstractType?> MaybeAbrupt<V>.neverAbrupt() =
    returnIfAbrupt { neverHappens() }
