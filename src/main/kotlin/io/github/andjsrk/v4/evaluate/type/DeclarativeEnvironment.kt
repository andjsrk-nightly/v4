package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

@EsSpec("Declarative Environment Record")
open class DeclarativeEnvironment(outer: Environment?): Environment(outer) {
    val bindings = mutableMapOf<String, Binding>()
    override fun hasBinding(name: String) =
        name in bindings
    override fun createMutableBinding(name: String): Completion {
        assert(name !in bindings)
        bindings[name] = Binding(true, null)
        return Completion.empty
    }
    override fun createImmutableBinding(name: String): Completion {
        assert(name !in bindings)
        bindings[name] = Binding(false, null)
        return Completion.empty
    }
    override fun initializeBinding(name: String, value: LanguageType): Completion {
        val binding = bindings[name]
        assert(binding != null && binding.not { isInitialized })
        requireNotNull(binding)
        binding.value = value
        return Completion.empty
    }
    override fun setMutableBinding(name: String, value: LanguageType): Completion {
        val binding = bindings[name] ?: return Completion.`throw`(NullType/* ReferenceError */)
        if (binding.not { isInitialized }) return Completion.`throw`(NullType/* ReferenceError */)
        if (binding.not { isMutable }) return Completion.`throw`(NullType/* TypeError */)
        binding.value = value
        return Completion.empty
    }
    override fun getValue(name: String): Completion {
        assert(name in bindings)
        val binding = bindings[name] ?: neverHappens()
        if (binding.not { isInitialized }) return Completion.`throw`(NullType/* ReferenceError */)
        return Completion.normal(binding.value!!)
    }
}
