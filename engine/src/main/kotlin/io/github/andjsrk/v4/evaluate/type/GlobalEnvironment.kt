package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.lang.*

class GlobalEnvironment(global: ObjectType): Environment(null) {
    val declarative = DeclarativeEnvironment(null)
    val `object` = ObjectEnvironment(global, null)
    override fun hasBinding(name: String): MaybeAbrupt<BooleanType> {
        val declarativeHas = declarative.hasBinding(name).value
        val objectHas = `object`.hasBinding(name)
            .orReturn { return it }
        return (declarativeHas or objectHas)
            .toNormal()
    }
    override fun createMutableBinding(name: String) =
        if (declarative.hasBinding(name).value.value) TODO()
        else declarative.createMutableBinding(name)
    override fun createImmutableBinding(name: String) =
        if (declarative.hasBinding(name).value.value) TODO()
        else declarative.createImmutableBinding(name)
    override fun initializeBinding(name: String, value: LanguageType) =
        ifHasBinding(name) {
            it.initializeBinding(name, value)
        }
    override fun setMutableBinding(name: String, value: LanguageType) =
        ifHasBinding(name) {
            it.setMutableBinding(name, value)
        }
    override fun getBindingValue(name: String) =
        ifHasBinding(name) {
            it.getBindingValue(name)
        }
    private inline fun <R: AbstractType?> ifHasBinding(name: String, task: (Environment) -> Completion<R>) =
        if (declarative.hasBinding(name).value.value) task(declarative)
        else task(`object`)
}
