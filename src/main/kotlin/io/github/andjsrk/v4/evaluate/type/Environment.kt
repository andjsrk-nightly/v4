package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Environment Record")
sealed class Environment(var outer: Environment?): Record {
    @EsSpec("HasBinding")
    abstract fun hasBinding(name: String): Boolean
    @EsSpec("CreateMutableBinding")
    abstract fun createMutableBinding(name: String): EmptyOrAbrupt
    @EsSpec("CreateImmutableBinding")
    abstract fun createImmutableBinding(name: String): EmptyOrAbrupt
    @EsSpec("InitializeBinding")
    abstract fun initializeBinding(name: String, value: LanguageType): EmptyOrAbrupt
    @EsSpec("SetMutableBinding")
    abstract fun setMutableBinding(name: String, value: LanguageType): EmptyOrAbrupt
    @EsSpec("GetBindingValue")
    abstract fun getValue(name: String): NonEmptyNormalOrAbrupt
}
