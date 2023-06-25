package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.Reference
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal fun Environment?.putOrInitializeBinding(ref: Reference, value: LanguageType) =
    if (this == null) ref.putValue(value)
    else ref.initializeBinding(value)
