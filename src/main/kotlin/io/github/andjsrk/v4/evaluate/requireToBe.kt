package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <reified T: LanguageType> LanguageType.requireToBe(`return`: CompletionReturn) {
    contract {
        returns() implies (this@requireToBe is T)
    }

    if (this !is T) `return`(Completion.Throw(NullType/* TypeError */))
}
