package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("InitializeBoundName")
internal fun initializeBoundName(name: StringType, value: LanguageType, env: DeclarativeEnvironment?) =
    if (env != null) env.initializeBinding(name.value, value)
    else neverAbrupt(resolveBinding(name)).putValue(value)
