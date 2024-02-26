package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.evaluate.*

class ObjectEnvironment(val `object`: ObjectType, outer: Environment?): Environment(outer) {
    override fun hasBinding(name: String) =
        `object`.hasProperty(name.languageValue)
    override fun createMutableBinding(name: String) =
        `object`.definePropertyOrThrow(name.languageValue, DataProperty(NullType))
    /**
     * Indicates `CreateMutableBinding` with argument `D` that is set to `false`.
     */
    @EsSpec("CreateMutableBinding")
    fun createNonConfigurableMutableBinding(name: String) =
        `object`.definePropertyOrThrow(name.languageValue, DataProperty(NullType, configurable=false))
    override fun createImmutableBinding(name: String) =
        throw NotImplementedError()
    override fun initializeBinding(name: String, value: LanguageType) =
        setMutableBinding(name, value)
    override fun setMutableBinding(name: String, value: LanguageType): EmptyOrThrow {
        val has = `object`.hasProperty(name.languageValue)
            .orReturnThrow { return it }
            .nativeValue
        if (!has) return throwError(ReferenceErrorKind.NOT_DEFINED, name)
        `object`.set(name.languageValue, value)
            .orReturnThrow { return it }
        return empty
    }
    override fun getBindingValue(name: String): NonEmptyOrThrow {
        val has = `object`.hasProperty(name.languageValue)
            .orReturnThrow { return it }
            .nativeValue
        if (!has) return throwError(ReferenceErrorKind.NOT_DEFINED, name)
        return `object`.get(name.languageValue)
    }
}
