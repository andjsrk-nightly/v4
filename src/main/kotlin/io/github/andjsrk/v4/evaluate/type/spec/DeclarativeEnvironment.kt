package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

@EsSpec("Declarative Environment Record")
open class DeclarativeEnvironment(outer: Environment? = null): Environment(outer) {
    val bindings = mutableMapOf<String, Binding>()
    @EsSpec("HasBinding")
    override fun contains(name: String) =
        name in bindings
    @EsSpec("CreateMutableBinding")
    fun createMutableBinding(name: String) {
        assert(name !in bindings)
        bindings[name] = Binding(true, null)
    }
    @EsSpec("CreateImmutableBinding")
    fun createImmutableBinding(name: String) {
        assert(name !in bindings)
        bindings[name] = Binding(false, null)
    }
    @EsSpec("InitializeBinding")
    override fun initializeBinding(name: String, value: LanguageType) {
        val binding = bindings[name]
        assert(binding != null && binding.not { isInitialized })
        requireNotNull(binding)
        binding.value = value
    }
    @EsSpec("SetMutableBinding")
    fun setMutableBinding(name: String, value: LanguageType): Completion {
        val binding = bindings[name] ?: return Completion(Completion.Type.THROW, NullType/* ReferenceError */)
        if (binding.not { isInitialized }) return Completion(Completion.Type.THROW, NullType/* ReferenceError */)
        if (binding.not { isMutable }) return Completion(Completion.Type.THROW, NullType/* TypeError */)
        binding.value = value
        return Completion.normal(NullType)
    }
    override fun getValue(name: String): Completion {
        assert(name in bindings)
        val binding = bindings[name] ?: neverHappens()
        if (binding.not { isInitialized }) return Completion(Completion.Type.THROW, NullType/* ReferenceError */)
        return Completion.normal(binding.value!!)
    }
}
