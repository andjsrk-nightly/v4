package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Environment Record")
sealed class Environment(open val outer: Environment?): Record {
    @EsSpec("HasBinding")
    abstract fun hasBinding(name: String): MaybeThrow<BooleanType>
    @EsSpec("CreateMutableBinding")
    abstract fun createMutableBinding(name: String): EmptyOrThrow
    @EsSpec("CreateImmutableBinding")
    abstract fun createImmutableBinding(name: String): EmptyOrThrow
    @EsSpec("InitializeBinding")
    abstract fun initializeBinding(name: String, value: LanguageType): EmptyOrThrow
    @EsSpec("SetMutableBinding")
    abstract fun setMutableBinding(name: String, value: LanguageType): EmptyOrThrow
    @EsSpec("GetBindingValue")
    abstract fun getBindingValue(name: String): NonEmptyOrThrow
}
