package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion

@EsSpec("LoopContinues")
fun continueLoop(completion: Completion<*>) =
    when (completion) {
        is Completion.WideNormal<*> -> true
        !is Completion.Continue -> false
        // TODO: label
        else -> true
    }
