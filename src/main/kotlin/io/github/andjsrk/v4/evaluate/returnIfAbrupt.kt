package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

/**
 * Indicates a lambda that returns an abrupt completion, which will be used on inline functions.
 */
internal typealias AbruptReturnLambda = (Completion.Abrupt) -> Nothing

@EsSpec("ReturnIfAbrupt")
internal inline fun <V: AbstractType?> MaybeAbrupt<V>.returnIfAbrupt(`return`: AbruptReturnLambda) =
    when (this) {
        is Completion.WideNormal<V> -> value
        is Completion.Abrupt -> `return`(this)
    }

internal inline fun <V: AbstractType?> neverAbrupt(completion: MaybeAbrupt<V>) =
    completion.returnIfAbrupt { neverHappens() }
