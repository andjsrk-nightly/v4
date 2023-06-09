package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.not

@EsSpec("UpdateEmpty")
internal fun updateEmpty(completion: Completion, value: AbstractType?): Completion {
    if (completion.not { isEmpty }) return completion

    if (value == null) return completion.copy(value=NullType, isEmpty=true)
    else return completion.copy(value=value, isEmpty=false)
}
