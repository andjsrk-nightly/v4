package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion

@EsSpec("LoopContinues")
val Completion<*>.continuesLoop get() =
    when (this) {
        is Completion.WideNormal<*> -> true
        !is Completion.Continue -> false
        // TODO: label
        else -> true
    }
