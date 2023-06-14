package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.spec.*

class ObjectType(
    val properties: MutableMap<PropertyKey, Property>,
    prototype: ObjectType? = null,
): LanguageType {
    var prototype = prototype
        private set
    var extensible = true

    @EsSpec("[[SetPrototypeOf]]")
    fun setPrototype(prototype: ObjectType?): WasSuccessful {
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
    @EsSpec("HasOwnProperty")
    fun hasOwnProperty(key: PropertyKey) =
        getOwnProperty(key) != null
    @EsSpec("[[GetOwnProperty]]")
    fun getOwnProperty(key: PropertyKey): Property? {
        val found = properties.keys.find { equal(it, key).value } ?: return null
        return properties[found]
    }
    @EsSpec("[[DefineOwnProperty]]")
    fun defineOwnProperty(key: PropertyKey, descriptor: Property): WasSuccessful {
        val current = getOwnProperty(key)
        return applyPropertyDescriptor(key, descriptor, current)
    }
    @EsSpec("IsCompatiblePropertyDescriptor")
    fun isCompatiblePropertyDescriptor(descriptor: Property, current: Property?): Boolean {
        if (current == null) return extensible
        if (current.not { configurable }) {
            if (descriptor.configurable) return false
            if (descriptor.enumerable != current.enumerable) return false
            if (current::class != descriptor::class) return false
            if (current is AccessorProperty) {
                require(descriptor is AccessorProperty)
                if (!sameNullableValue(current.get, descriptor.get)) return false
                if (!sameNullableValue(current.set, descriptor.set)) return false
            } else {
                require(current is DataProperty)
                if (current.not { writable } && descriptor is DataProperty) {
                    if (descriptor.writable) return false
                    if (!sameValue(current.value, descriptor.value).value) return false
                }
            }
        }
        return true
    }
    fun applyPropertyDescriptor(key: PropertyKey, descriptor: Property, current: Property?): WasSuccessful {
        if (!isCompatiblePropertyDescriptor(descriptor, current)) return false

        properties[key] = when (descriptor) {
            is AccessorProperty -> descriptor.copy()
            is DataProperty -> descriptor.copy()
        }

        return true
    }
    @EsSpec("[[HasProperty]]")
    fun hasProperty(key: PropertyKey): Boolean =
        hasOwnProperty(key) || prototype?.hasProperty(key) ?: false
    @EsSpec("[[Get]]")
    fun get(key: PropertyKey, receiver: ObjectType? = null): LanguageType {
        val descriptor = getOwnProperty(key)
        if (descriptor == null) {
            val proto = prototype ?: return NullType
            return proto.get(key, receiver)
        }
        if (descriptor is DataProperty) return descriptor.value
        require(descriptor is AccessorProperty)
        val getter = descriptor.get ?: return NullType
        TODO()
    }
    fun set(key: PropertyKey, value: LanguageType, receiver: ObjectType? = null): WasSuccessful {
        TODO()
    }
    fun delete(key: PropertyKey): WasSuccessful {
        TODO()
    }
    fun ownProperties() =
        properties.keys.toList()

    companion object {
        fun createBasic(): ObjectType =
            ObjectType(mutableMapOf())
        @EsSpec("OrdinaryObjectCreate")
        fun create(prototype: ObjectType?) =
            ObjectType(mutableMapOf(), prototype)
    }
}
