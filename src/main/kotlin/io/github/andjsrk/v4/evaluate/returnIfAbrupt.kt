package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.neverHappens

@EsSpec("ReturnIfAbrupt")
internal inline fun returnIfAbrupt(completion: Completion, `return`: (Completion) -> Nothing) =
    if (completion.type.isAbrupt) `return`(completion)
    else completion.value

@EsSpec("ReturnIfAbrupt")
@JvmName("returnIfAbruptWithCast")
internal inline fun <reified R: AbstractType> returnIfAbrupt(completion: Completion, `return`: (Completion) -> Nothing) =
    returnIfAbrupt(completion, `return`) as R

internal inline fun neverAbrupt(completion: Completion) =
    returnIfAbrupt(completion) { neverHappens() }

@JvmName("neverAbruptWithCast")
internal inline fun <reified R> neverAbrupt(completion: Completion) =
    neverAbrupt(completion) as R
