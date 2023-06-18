package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Environment Record")
sealed class Environment(var outer: Environment?): Record {
    @EsSpec("HasBinding")
    abstract fun hasBinding(name: String): Boolean
    @EsSpec("CreateMutableBinding")
    abstract fun createMutableBinding(name: String): Completion
    /**
     * Indicates `CreateMutableBinding` with argument `D` that is set to `false`.
     */
    @EsSpec("CreateMutableBinding")
    open fun createNonConfigurableMutableBinding(name: String) =
        createMutableBinding(name)
    @EsSpec("CreateImmutableBinding")
    abstract fun createImmutableBinding(name: String): Completion
    @EsSpec("InitializeBinding")
    abstract fun initializeBinding(name: String, value: LanguageType): Completion
    @EsSpec("SetMutableBinding")
    abstract fun setMutableBinding(name: String, value: LanguageType): Completion
    @EsSpec("GetBindingValue")
    abstract fun getValue(name: String): Completion
}
