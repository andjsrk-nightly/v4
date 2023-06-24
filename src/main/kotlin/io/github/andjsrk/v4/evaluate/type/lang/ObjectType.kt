package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.type.*

/**
 * Note that methods which its name start with underscore means it is an internal method in ES specification.
 */
open class ObjectType(
    prototype: PrototypeObject? = null,
    val properties: MutableMap<PropertyKey, Property> = mutableMapOf(),
): LanguageType {
    var prototype = prototype
        protected set
    var extensible = true
        protected set

    @EsSpec("[[SetPrototypeOf]]")
    fun _setPrototype(prototype: PrototypeObject?): WasSuccessful {
        val curr = this.prototype
        if (!extensible) return false
        var proto: PrototypeObject? = prototype
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
    fun _defineOwnProperty(key: PropertyKey, descriptor: Property): EmptyOrAbrupt {
        val current = _getOwnProperty(key)
        return _applyPropertyDescriptor(key, descriptor, current)
    }
    /**
     * Returns a normal completion containing empty or a throw completion.
     */
    fun _throwIfNotCompatiblePropertyDescriptor(current: Property?): EmptyOrAbrupt {
        when {
            current == null -> if (!extensible) return Completion.Throw(NullType/* TypeError */)
            current.not { configurable } -> return Completion.Throw(NullType/* TypeError */)
        }
        return empty
    }
    @EsSpec("IsCompatiblePropertyDescriptor")
    fun _isCompatiblePropertyDescriptor(current: Property?): Boolean {
        if (current == null) return extensible
        return current.configurable
    }
    fun _applyPropertyDescriptor(key: PropertyKey, descriptor: Property, current: Property?): EmptyOrAbrupt {
        returnIfAbrupt(_throwIfNotCompatiblePropertyDescriptor(current)) { return it }

        properties[key] = when (descriptor) {
            is AccessorProperty -> descriptor.copy()
            is DataProperty -> descriptor.copy()
        }

        return empty
    }
    @EsSpec("[[HasProperty]]")
    fun _hasProperty(key: PropertyKey): Boolean =
        hasOwnProperty(key) || prototype?._hasProperty(key) ?: false
    @EsSpec("[[Get]]")
    fun _get(key: PropertyKey, receiver: LanguageType): NonEmptyNormalOrAbrupt {
        val descriptor = _getOwnProperty(key)
        if (descriptor == null) {
            val proto = prototype ?: return Completion.Normal.`null`
            return proto._get(key, receiver)
        }
        if (descriptor is DataProperty) return Completion.Normal(descriptor.value)
        require(descriptor is AccessorProperty)
        val getter = descriptor.get ?: return Completion.Normal.`null`
        TODO()
    }
    @EsSpec("[[Set]]")
    fun _set(key: PropertyKey, value: LanguageType, receiver: LanguageType): MaybeAbrupt<BooleanType?> {
        when (val ownDesc = _getOwnProperty(key)) {
            null -> {
                val parent = prototype
                if (parent != null) parent._set(key, value, receiver)
                else properties[key] = DataProperty(value)
            }
            is DataProperty -> {
                if (ownDesc.not { writable }) return Completion.Throw(NullType/* TypeError */)
                if (receiver !is ObjectType) return Completion.Throw(NullType/* TypeError */)
                val existingDesc = receiver._getOwnProperty(key)
                if (existingDesc == null) createDataProperty(key, value)
                else {
                    if (existingDesc is AccessorProperty) return Completion.Normal(BooleanType.FALSE)
                    require(existingDesc is DataProperty)
                    if (existingDesc.not { writable }) return Completion.Throw(NullType/* TypeError */)
                    receiver._defineOwnProperty(key, existingDesc.copy(value=value))
                }
            }
            is AccessorProperty -> {
                val setter = ownDesc.set ?: return Completion.Throw(NullType/*  */)
                TODO()
            }
        }
        return empty
    }
    @EsSpec("[[Delete]]")
    fun _delete(key: PropertyKey): EmptyOrAbrupt {
        val desc = _getOwnProperty(key) ?: return empty
        if (desc.not { configurable }) return Completion.Throw(NullType/* TypeError */)
        properties.remove(key)
        return empty
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
    fun setImmutabilityLevel(level: ObjectImmutabilityLevel): EmptyOrAbrupt {
        val keys = _ownPropertyKeys()
        when (level) {
            ObjectImmutabilityLevel.SEALED -> {
                for (key in keys) {
                    val desc = _getOwnProperty(key)!!
                        .clone()
                        .apply {
                            configurable = false
                        }
                    returnIfAbrupt(definePropertyOrThrow(key, desc)) { return it }
                }
            }
            ObjectImmutabilityLevel.FROZEN -> {
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
        return empty
    }
    @EsSpec("TestIntegrityLevel")
    fun satisfiesImmutabilityLevel(level: ObjectImmutabilityLevel): Boolean {
        if (extensible) return false
        for (key in _ownPropertyKeys()) {
            val desc = _getOwnProperty(key)!!
            if (desc.configurable) return false
            if (level == ObjectImmutabilityLevel.FROZEN && desc is DataProperty) {
                if (desc.writable) return false
            }
        }
        return true
    }

    companion object {
        @EsSpec("MakeBasicObject")
        fun createBasic() =
            // TODO: fix if needed
            ObjectType()
        /**
         * Returns an Object that `[[Prototype]]` is set to `%Object.prototype%`.
         */
        fun createNormal() =
            ObjectType(Object.instancePrototype)
        @EsSpec("OrdinaryObjectCreate")
        fun create(prototype: PrototypeObject?) =
            ObjectType(prototype)
    }
}
