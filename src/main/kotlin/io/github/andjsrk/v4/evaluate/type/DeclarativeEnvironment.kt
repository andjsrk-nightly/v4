package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

@EsSpec("Declarative Environment Record")
open class DeclarativeEnvironment(outer: Environment?): Environment(outer) {
    val bindings = mutableMapOf<String, Binding>()
    override fun hasBinding(name: String) =
        name in bindings
    override fun createMutableBinding(name: String): Empty {
        assert(name !in bindings)
        bindings[name] = Binding(true, null)
        return Completion.Normal.empty
    }
    override fun createImmutableBinding(name: String): Empty {
        assert(name !in bindings)
        bindings[name] = Binding(false, null)
        return Completion.Normal.empty
    }
    override fun initializeBinding(name: String, value: LanguageType): Empty {
        val binding = bindings[name]
        assert(binding != null && binding.not { isInitialized })
        requireNotNull(binding)
        binding.value = value
        return Completion.Normal.empty
    }
    override fun setMutableBinding(name: String, value: LanguageType): EmptyOrAbrupt {
        val binding = bindings[name] ?: return Completion.Throw(NullType/* ReferenceError */)
        if (binding.not { isInitialized }) return Completion.Throw(NullType/* ReferenceError */)
        if (binding.not { isMutable }) return Completion.Throw(NullType/* TypeError */)
        binding.value = value
        return Completion.Normal.empty
    }
    override fun getValue(name: String): NonEmptyNormalOrAbrupt {
        assert(name in bindings)
        val binding = bindings[name] ?: neverHappens()
        if (binding.not { isInitialized }) return Completion.Throw(NullType/* ReferenceError */)
        return Completion.Normal(binding.value!!)
    }
}
