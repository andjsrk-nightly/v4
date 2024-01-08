package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.HostConfig
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

internal fun LanguageType.display(raw: Boolean = false) =
    HostConfig.value.display(this, raw)
