package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType
import io.github.andjsrk.v4.evaluate.type.spec.*

@EsSpec("InitializeBoundName")
internal fun initializeBoundName(name: StringType, value: LanguageType, env: DeclarativeEnvironment?): Completion {
    if (env != null) {
        env.initializeBinding(name.value, value)
        return Completion.empty
    } else {
        val lhs = returnIfAbrupt<Reference>(resolveBinding(name)) { return it }
        return lhs.putValue(value)
    }
}
