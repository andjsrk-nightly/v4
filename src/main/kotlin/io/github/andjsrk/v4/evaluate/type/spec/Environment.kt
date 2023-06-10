package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Environment Record")
sealed class Environment(var outer: Environment? = null): Record {
    @EsSpec("HasBinding")
    abstract operator fun contains(name: String): Boolean
    @EsSpec("InitializeBinding")
    abstract fun initializeBinding(name: String, value: LanguageType)
    @EsSpec("GetBindingValue")
    abstract fun getValue(name: String): Completion
}
