package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.neverHappens

@EsSpec("UpdateEmpty")
internal fun <C: Completion<*>> updateEmpty(completion: C, value: AbstractType?): C =
    if (completion.value != null) completion
    else when (completion) {
        is Completion.Normal<*> -> {
            require(value is LanguageType?)
            Completion.Normal(value)
        }
        is Completion.WideNormal<*> -> Completion.WideNormal(value)
        is Completion.Continue -> {
            require(value is LanguageType?)
            Completion.Continue(value, completion.target)
        }
        is Completion.Break -> {
            require(value is LanguageType?)
            Completion.Break(value, completion.target)
        }
        else -> neverHappens() // return completion and throw completion are non-empty
    } as C
