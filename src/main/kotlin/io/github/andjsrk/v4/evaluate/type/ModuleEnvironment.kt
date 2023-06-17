package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.not

@EsSpec("Module Environment Record")
class ModuleEnvironment(outer: Environment? = null): DeclarativeEnvironment(outer) {
    override fun getValue(name: String): Completion {
        val binding = bindings[name]
        assert(binding != null)
        requireNotNull(binding)
        // TODO: implement step 3
        if (binding.not { isInitialized }) return Completion.`throw`(NullType/* ReferenceError */)
        return Completion.normal(binding.value!!)
    }
    fun createImportBinding(name: String, module: Module, ) {
        TODO()
    }
}
