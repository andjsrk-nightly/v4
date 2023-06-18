package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.*

class GlobalEnvironment(global: ObjectType): Environment(null) {
    val declarative = DeclarativeEnvironment(null)
    val `object` = ObjectEnvironment(global, null)
    override fun hasBinding(name: String) =
        declarative.hasBinding(name) || `object`.hasBinding(name)
    override fun createMutableBinding(name: String) =
        if (declarative.hasBinding(name)) Completion.`throw`(NullType/* TypeError */)
        else declarative.createMutableBinding(name)
    override fun createImmutableBinding(name: String) =
        if (declarative.hasBinding(name)) Completion.`throw`(NullType/* TypeError */)
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
    private inline fun ifHasBinding(name: String, task: (Environment) -> Completion) =
        if (declarative.hasBinding(name)) task(declarative)
        else task(`object`)
}