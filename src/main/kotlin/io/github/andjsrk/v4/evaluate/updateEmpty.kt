package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.not

@EsSpec("UpdateEmpty")
internal fun updateEmpty(completion: Completion, value: AbstractType) =
    if (completion.not { isEmpty }) completion
    else completion.copy(value=value, isEmpty=false)
