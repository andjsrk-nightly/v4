package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.neverHappens

internal typealias CompletionReturn = (Completion) -> Nothing

@EsSpec("ReturnIfAbrupt")
internal inline fun returnIfAbrupt(completion: Completion, `return`: CompletionReturn) =
    if (completion.isAbrupt) `return`(completion)
    else completion.value

@EsSpec("ReturnIfAbrupt")
@JvmName("returnIfAbruptWithCast")
internal inline fun <reified R: AbstractType?> returnIfAbrupt(completion: Completion, `return`: CompletionReturn) =
    returnIfAbrupt(completion, `return`) as R

internal inline fun getLanguageTypeOrReturn(completion: Completion, `return`: CompletionReturn) =
    returnIfAbrupt<LanguageType>(completion, `return`)

internal inline fun neverAbrupt(completion: Completion) =
    returnIfAbrupt(completion) { neverHappens() }

@JvmName("neverAbruptWithCast")
internal inline fun <reified R: AbstractType?> neverAbrupt(completion: Completion) =
    neverAbrupt(completion) as R
