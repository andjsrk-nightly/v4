package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

@EsSpec("GetValue")
internal fun getValue(v: AbstractType): Completion {
    return Completion.normal(v) // temp
}

/**
 * @see getValue
 * @see returnIfAbrupt
 */
internal inline fun getValueOrReturn(v: AbstractType, `return`: (Completion) -> Nothing) =
    returnIfAbrupt<LanguageType>(getValue(v), `return`)
