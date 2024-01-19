package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.not

@EsSpec("Module Environment Record")
class ModuleEnvironment(outer: Environment?): DeclarativeEnvironment(outer) {
    override fun getBindingValue(name: String): NonEmptyOrAbrupt {
        val binding = bindings[name]
        requireNotNull(binding)
        if (binding.isIndirect) {
            val targetEnv = binding.module!!.env
                ?: return throwError(TODO())
            return targetEnv.getBindingValue(binding.exportedLocalName!!)
        }
        if (binding.not { isInitialized }) return throwError(ReferenceErrorKind.ACCESSED_UNINITIALIZED_VARIABLE, name)
        return binding.value!!.toNormal()
    }
    fun createImportBinding(name: String, module: Module, bindingName: String) {
        assert(name !in bindings)
        bindings[name] = Binding(false, null, true, module, bindingName)
    }
}
