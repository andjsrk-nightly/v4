package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

@EsSpec("ReturnIfAbrupt")
inline fun returnIfAbrupt(completion: Completion, `return`: (Completion) -> Nothing) =
    if (completion.type.isAbrupt) `return`(completion)
    else completion.value

@EsSpec("ReturnIfAbrupt")
@JvmName("returnIfAbruptWithCast")
inline fun <reified R: AbstractType> returnIfAbrupt(completion: Completion, `return`: (Completion) -> Nothing) =
    returnIfAbrupt(completion, `return`) as R
