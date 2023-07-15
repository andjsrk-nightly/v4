package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod
import java.text.Normalizer
import java.text.Normalizer.Form

val normalize = builtinMethod("normalize") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val form = args.getOptional(1)
        ?.requireToBeString { return@fn it }
        ?: "NFC"
    val normalized = Normalizer.normalize(string, Form.valueOf(form))
    Completion.Normal(
        normalized.languageValue
    )
}
