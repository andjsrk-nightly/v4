package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.not

@EsSpec("Module Environment Record")
class ModuleEnvironment(outer: Environment?): DeclarativeEnvironment(outer) {
    override fun getValue(name: String): NonEmptyNormalOrAbrupt {
        val binding = bindings[name]
        assert(binding != null)
        requireNotNull(binding)
        // TODO: implement step 3
        if (binding.not { isInitialized }) return Completion.Throw(NullType/* ReferenceError */)
        return Completion.Normal(binding.value!!)
    }
    fun createImportBinding(name: String, module: SourceTextModule, ) {
        TODO()
    }
}
