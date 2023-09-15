package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

class GlobalEnvironment(global: ObjectType): Environment(null) {
    val declarative = DeclarativeEnvironment(null)
    val `object` = ObjectEnvironment(global, null)
    override fun hasBinding(name: String) =
        declarative.hasBinding(name) || `object`.hasBinding(name)
    override fun createMutableBinding(name: String) =
        if (declarative.hasBinding(name)) TODO()
        else declarative.createMutableBinding(name)
    override fun createImmutableBinding(name: String) =
        if (declarative.hasBinding(name)) TODO()
        else declarative.createImmutableBinding(name)
    override fun initializeBinding(name: String, value: LanguageType) =
        ifHasBinding(name) {
            it.initializeBinding(name, value)
        }
    override fun setMutableBinding(name: String, value: LanguageType) =
        ifHasBinding(name) {
            it.setMutableBinding(name, value)
        }
    override fun getValue(name: String) =
        ifHasBinding(name) {
            it.getValue(name)
        }
    private inline fun <R: AbstractType?> ifHasBinding(name: String, task: (Environment) -> Completion<R>) =
        if (declarative.hasBinding(name)) task(declarative)
        else task(`object`)
}
