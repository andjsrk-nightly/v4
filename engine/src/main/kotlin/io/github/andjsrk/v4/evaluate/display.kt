package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.HostConfig
import io.github.andjsrk.v4.evaluate.type.LanguageType

internal fun LanguageType.display(raw: Boolean = true) =
    HostConfig.value.display(this, raw)
