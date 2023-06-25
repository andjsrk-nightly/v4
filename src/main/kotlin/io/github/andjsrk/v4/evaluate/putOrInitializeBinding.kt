package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.Reference
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

/**
 * Note that the function accepts a [Reference], not a [String].
 */
@EsSpec("InitializeBoundName")
internal fun Reference.putOrInitializeBinding(value: LanguageType, env: Environment?) =
    if (env == null) putValue(value)
    else initializeBinding(value)
