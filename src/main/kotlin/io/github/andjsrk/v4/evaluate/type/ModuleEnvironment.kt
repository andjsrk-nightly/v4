package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.not

@EsSpec("Module Environment Record")
class ModuleEnvironment(outer: Environment?): DeclarativeEnvironment(outer) {
    override fun getValue(name: String): NonEmptyNormalOrAbrupt {
        val binding = bindings[name]
        assert(binding != null)
        requireNotNull(binding)
        // TODO: implement step 3
        if (binding.not { isInitialized }) return throwError(ReferenceErrorKind.ACCESSED_UNINITIALIZED_VARIABLE, name)
        return Completion.Normal(binding.value!!)
    }
    fun createImportBinding(name: String, module: SourceTextModule, ) {
        TODO()
    }
}
