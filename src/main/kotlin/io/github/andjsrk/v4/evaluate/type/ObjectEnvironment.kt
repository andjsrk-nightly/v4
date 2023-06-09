package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

class ObjectEnvironment(val `object`: ObjectType, outer: Environment?): Environment(outer) {
    override fun hasBinding(name: String) =
        `object`.hasProperty(name.languageValue)
    override fun createMutableBinding(name: String) =
        `object`.definePropertyOrThrow(name.languageValue, DataProperty(NullType))
    override fun createNonConfigurableMutableBinding(name: String) =
        `object`.definePropertyOrThrow(name.languageValue, DataProperty(NullType, configurable=false))
    override fun createImmutableBinding(name: String) =
        throw NotImplementedError()
    override fun initializeBinding(name: String, value: LanguageType) =
        setMutableBinding(name, value)
    override fun setMutableBinding(name: String, value: LanguageType): EmptyOrAbrupt {
        if (`object`.not { hasProperty(name.languageValue) }) return throwError(ReferenceErrorKind.NOT_DEFINED, name)
        `object`.set(name.languageValue, value)
            .returnIfAbrupt { return it }
        return empty
    }
    override fun getValue(name: String): NonEmptyNormalOrAbrupt {
        if (`object`.not { hasProperty(name.languageValue) }) return throwError(ReferenceErrorKind.NOT_DEFINED, name)
        return `object`.get(name.languageValue)
    }
}
