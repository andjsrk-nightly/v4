package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Declarative Environment Record")
open class DeclarativeEnvironment(outer: Environment?): Environment(outer) {
    val bindings = mutableMapOf<String, Binding>()
    override fun hasBinding(name: String) =
        name in bindings
    override fun createMutableBinding(name: String): Empty {
        assert(name !in bindings)
        bindings[name] = Binding(true, null)
        return empty
    }
    override fun createImmutableBinding(name: String): Empty {
        assert(name !in bindings)
        bindings[name] = Binding(false, null)
        return empty
    }
    override fun initializeBinding(name: String, value: LanguageType): Empty {
        val binding = bindings[name]
        assert(binding != null && binding.not { isInitialized })
        requireNotNull(binding)
        binding.value = value
        return empty
    }
    override fun setMutableBinding(name: String, value: LanguageType): EmptyOrAbrupt {
        val binding = bindings[name] ?: return throwError(ReferenceErrorKind.NOT_DEFINED, name)
        if (binding.not { isInitialized }) return throwError(ReferenceErrorKind.ACCESSED_UNINITIALIZED_VARIABLE, name)
        if (binding.not { isMutable }) return throwError(TypeErrorKind.CONST_ASSIGN)
        binding.value = value
        return empty
    }
    override fun getValue(name: String): NonEmptyNormalOrAbrupt {
        assert(name in bindings)
        val binding = bindings[name] ?: neverHappens()
        if (binding.not { isInitialized }) return throwError(ReferenceErrorKind.ACCESSED_UNINITIALIZED_VARIABLE, name)
        return binding.value!!.toNormal()
    }
}
