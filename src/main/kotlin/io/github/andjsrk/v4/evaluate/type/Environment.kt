package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Environment Record")
sealed class Environment(var outer: Environment? = null): Record {
    @EsSpec("HasBinding")
    abstract operator fun contains(name: String): Boolean
    @EsSpec("InitializeBinding")
    abstract fun initializeBinding(name: String, value: LanguageType)
    @EsSpec("SetMutableBinding")
    abstract fun setMutableBinding(name: String, value: LanguageType): Completion
    @EsSpec("GetBindingValue")
    abstract fun getValue(name: String): Completion
}
