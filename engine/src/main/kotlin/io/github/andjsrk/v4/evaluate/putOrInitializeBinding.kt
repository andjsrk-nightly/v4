package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

/**
 * Note that the function accepts a [Reference], not a [String].
 */
@EsSpec("InitializeBoundName")
internal fun Reference.putOrInitializeBinding(value: LanguageType, env: Environment?): EmptyOrThrow =
    if (env == null) putValue(value)
    else initializeBinding(value)
