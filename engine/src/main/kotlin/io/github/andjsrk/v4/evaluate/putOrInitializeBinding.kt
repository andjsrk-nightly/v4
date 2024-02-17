package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

/**
 * Note that the function accepts a [Reference], not a [String].
 */
@EsSpec("InitializeBoundName")
internal fun Reference.putOrInitializeBinding(value: LanguageType, env: Environment?): @CompilerFalsePositive EmptyOrThrow =
    if (env == null) putValue(value)
    else initializeBinding(value)
