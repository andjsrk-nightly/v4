package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
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
    override fun setMutableBinding(name: String, value: LanguageType): Completion {
        if (`object`.not { hasProperty(name) }) return Completion.`throw`(NullType/* ReferenceError */)
        returnIfAbrupt(`object`.set(name.languageValue, value)) { return it }
        return Completion.empty
    }
    override fun getValue(name: String): Completion {
        if (`object`.not { hasProperty(name) }) return Completion.`throw`(NullType/* ReferenceError */)
        return `object`.get(name.languageValue)
    }
}