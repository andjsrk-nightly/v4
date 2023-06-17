package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion

@EsSpec("LoopContinues")
val Completion.continuesLoop get() =
    when {
        type == Completion.Type.NORMAL -> true
        type != Completion.Type.CONTINUE -> false
        // TODO: label
        else -> true
    }
