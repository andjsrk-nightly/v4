package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.spec.*

/**
 * Note that methods which its name start with underscore means it is an internal method in ES specification.
 */
open class ObjectType(
    val properties: MutableMap<PropertyKey, Property>,
    prototype: ObjectType? = null,
): LanguageType {
    var prototype = prototype
        private set
    var extensible = true
        private set

    @EsSpec("[[SetPrototypeOf]]")
    fun _setPrototype(prototype: ObjectType?): WasSuccessful {
        val curr = this.prototype
        if (!extensible) return false
        var proto: ObjectType? = prototype
        while (true) {
            if (proto == null) break
            else if (curr != null && sameValue(curr, proto).value) return false
            else proto = proto.prototype
        }
        this.prototype = prototype
        return true
    }
    @EsSpec("[[GetOwnProperty]]")
    fun _getOwnProperty(key: PropertyKey) =
        properties[key]
    @EsSpec("[[DefineOwnProperty]]")
    fun _defineOwnProperty(key: PropertyKey, descriptor: Property): Completion {
        val current = _getOwnProperty(key)
        return _applyPropertyDescriptor(key, descriptor, current)
    }
    /**
     * Returns a normal completion containing empty or a throw completion.
     */
    fun _throwIfNotCompatiblePropertyDescriptor(current: Property?): Completion {
        when {
            current == null -> if (!extensible) return Completion.`throw`(NullType/* TypeError */)
            current.not { configurable } -> return Completion.`throw`(NullType/* TypeError */)
        }
        return Completion.empty
    }
    @EsSpec("IsCompatiblePropertyDescriptor")
    fun _isCompatiblePropertyDescriptor(current: Property?): Boolean {
        if (current == null) return extensible
        return current.configurable
    }
    fun _applyPropertyDescriptor(key: PropertyKey, descriptor: Property, current: Property?): Completion {
        returnIfAbrupt(_throwIfNotCompatiblePropertyDescriptor(current)) { return it }

        properties[key] = when (descriptor) {
            is AccessorProperty -> descriptor.copy()
            is DataProperty -> descriptor.copy()
        }

        return Completion.empty
    }
    @EsSpec("[[HasProperty]]")
    fun _hasProperty(key: PropertyKey): Boolean =
        hasOwnProperty(key) || prototype?._hasProperty(key) ?: false
    @EsSpec("[[Get]]")
    fun _get(key: PropertyKey, receiver: LanguageType): Completion {
        val descriptor = _getOwnProperty(key)
        if (descriptor == null) {
            val proto = prototype ?: return Completion.`null`
            return proto._get(key, receiver)
        }
        if (descriptor is DataProperty) return Completion.normal(descriptor.value)
        require(descriptor is AccessorProperty)
        val getter = descriptor.get ?: return Completion.`null`
        TODO()
    }
    @EsSpec("[[Set]]")
    fun _set(key: PropertyKey, value: LanguageType, receiver: LanguageType): Completion {
        when (val ownDesc = _getOwnProperty(key)) {
            null -> {
                val parent = prototype
                if (parent != null) parent._set(key, value, receiver)
                else properties[key] = DataProperty(value)
            }
            is DataProperty -> {
                if (ownDesc.not { writable }) return Completion.`throw`(NullType/* TypeError */)
                if (receiver !is ObjectType) return Completion.`throw`(NullType/* TypeError */)
                // TODO: clarify what `receiver` means and fix code if needed
                val existingDesc = receiver._getOwnProperty(key)
                if (existingDesc == null) createDataProperty(key, value)
                else {
                    if (existingDesc is AccessorProperty) return Completion.normal(BooleanType.FALSE)
                    require(existingDesc is DataProperty)
                    if (existingDesc.not { writable }) return Completion.`throw`(NullType/* TypeError */)
                    receiver._defineOwnProperty(key, existingDesc.copy(value=value))
                }
            }
            is AccessorProperty -> {
                val setter = ownDesc.set ?: return Completion.`throw`(NullType/*  */)
                TODO()
            }
        }
        return Completion.empty
    }
    @EsSpec("[[Delete]]")
    fun _delete(key: PropertyKey): Completion {
        val desc = _getOwnProperty(key) ?: return Completion.empty
        if (desc.not { configurable }) return Completion.`throw`(NullType/* TypeError */)
        properties.remove(key)
        return Completion.empty
    }
    @EsSpec("[[OwnPropertyKeys]]")
    fun _ownPropertyKeys() =
        // TODO: fix if needed
        properties.keys.toList()

    @EsSpec("Get")
    fun get(key: PropertyKey) =
        _get(key, this)
    // GetV is implemented as an extension for LanguageType
    @EsSpec("Set")
    fun set(key: PropertyKey, value: LanguageType) =
        _set(key, value, this)
    /**
     * Note that this function covers `CreateDataPropertyOrThrow` as well.
     */
    @EsSpec("CreateDataProperty")
    fun createDataProperty(key: PropertyKey, value: LanguageType) =
        _defineOwnProperty(key, DataProperty(value))
    @EsSpec("CreateMethodProperty")
    fun createMethodProperty(key: PropertyKey, value: LanguageType) {
        definePropertyOrThrow(key, DataProperty(value, writable=false, enumerable=false))
    }
    @EsSpec("CreateDataPropertyOrThrow")
    inline fun createDataPropertyOrThrow(key: PropertyKey, value: LanguageType) =
        createDataProperty(key, value)
    @EsSpec("CreateNonEnumerableDataPropertyOrThrow")
    fun createNonEnumerablePropertyOrThrow(key: PropertyKey, value: LanguageType) {
        definePropertyOrThrow(key, DataProperty(value, enumerable=false))
    }
    @EsSpec("DefinePropertyOrThrow")
    inline fun definePropertyOrThrow(key: PropertyKey, descriptor: Property) =
        _defineOwnProperty(key, descriptor)
    @EsSpec("DeletePropertyOrThrow")
    inline fun deletePropertyOrThrow(key: PropertyKey) =
        _delete(key)
    // GetMethod is implemented as an extension for LanguageType
    @EsSpec("HasProperty")
    inline fun hasProperty(key: PropertyKey) =
        _hasProperty(key)
    @EsSpec("HasOwnProperty")
    fun hasOwnProperty(key: PropertyKey) =
        _getOwnProperty(key) != null
    @EsSpec("SetIntegrityLevel")
    fun setImmutabilityLevel(level: ImmutabilityLevel): Completion {
        val keys = _ownPropertyKeys()
        when (level) {
            ImmutabilityLevel.SEALED -> {
                for (key in keys) {
                    val desc = _getOwnProperty(key)!!
                        .clone()
                        .apply {
                            configurable = false
                        }
                    returnIfAbrupt(definePropertyOrThrow(key, desc)) { return it }
                }
            }
            ImmutabilityLevel.FROZEN -> {
                for (key in keys) {
                    val desc = _getOwnProperty(key)!!.clone()
                        .apply {
                            configurable = false
                            if (this is DataProperty) writable = false
                        }
                    returnIfAbrupt(definePropertyOrThrow(key, desc)) { return it }
                }
            }
        }
        return Completion.empty
    }
    @EsSpec("TestIntegrityLevel")
    fun satisfiesImmutabilityLevel(level: ImmutabilityLevel): Boolean {
        if (extensible) return false
        for (key in _ownPropertyKeys()) {
            val desc = _getOwnProperty(key)!!
            if (desc.configurable) return false
            if (level == ImmutabilityLevel.FROZEN && desc is DataProperty) {
                if (desc.writable) return false
            }
        }
        return true
    }

    companion object {
        fun createBasic(): ObjectType =
            ObjectType(mutableMapOf())
        @EsSpec("OrdinaryObjectCreate")
        fun create(prototype: ObjectType?) =
            ObjectType(mutableMapOf(), prototype)
    }
}
