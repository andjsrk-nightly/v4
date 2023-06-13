package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

@EsSpec("UpdateEmpty")
internal fun updateEmpty(completion: Completion, value: AbstractType?) =
    if (completion.value != null) completion
    else completion.copy(value=value)
