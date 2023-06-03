package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

@EsSpec("UpdateEmpty")
fun <T: AbstractType?> updateEmpty(completion: Completion, value: T): Completion {
    // TODO: support empty
    val res = returnIfAbrupt(completion) { return it }
    return Completion.normal(res)
}
