package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.type.*

/**
 * Note that methods which its name start with underscore means it is an internal method in ES specification.
 *
 * @param lazyPrototype to avoid some recursions, its type is [Lazy].
 */
open class ObjectType(
    lazyPrototype: Lazy<PrototypeObjectType?> = lazy { Object.instancePrototype },
    val properties: MutableMap<PropertyKey, Property> = mutableMapOf(),
): LanguageType {
    var prototype by MutableLazy.from(lazyPrototype)
        protected set
    var extensible = true

    @EsSpec("[[SetPrototypeOf]]")
    fun _setPrototype(prototype: PrototypeObjectType?): WasSuccessful {
        val curr = this.prototype
        if (!extensible) return false
        var proto = prototype
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
    fun _throwIfNotCompatiblePropertyDescriptor(current: Property?, key: PropertyKey): EmptyOrAbrupt {
        when {
            current == null ->
                if (!extensible) return throwError(TypeErrorKind.OBJECT_NOT_EXTENSIBLE, key.string())
            current.not { configurable } ->
                return throwError(TypeErrorKind.CANNOT_REDEFINE, key.string())
        }
        return empty
    }
    fun _applyPropertyDescriptor(key: PropertyKey, descriptor: Property, current: Property?): EmptyOrAbrupt {
        returnIfAbrupt(_throwIfNotCompatiblePropertyDescriptor(current, key)) { return it }

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
                if (ownDesc.not { writable }) return throwError(TypeErrorKind.CANNOT_ASSIGN_TO_READ_ONLY_PROPERTY, key.string())
                require(receiver is ObjectType)
                val existingDesc = receiver._getOwnProperty(key)
                if (existingDesc == null) createDataProperty(key, value)
                else {
                    if (existingDesc is AccessorProperty) return Completion.Normal(BooleanType.FALSE)
                    require(existingDesc is DataProperty)
                    if (existingDesc.not { writable }) return throwError(TypeErrorKind.CANNOT_ASSIGN_TO_READ_ONLY_PROPERTY, key.string())
                    receiver._defineOwnProperty(key, existingDesc.copy(value=value))
                }
            }
            is AccessorProperty -> {
                val setter = ownDesc.set ?: return throwError(TypeErrorKind.NO_SETTER)
                TODO()
            }
        }
        return empty
    }
    @EsSpec("[[Delete]]")
    fun _delete(key: PropertyKey): EmptyOrAbrupt {
        val desc = _getOwnProperty(key) ?: return empty
        if (desc.not { configurable }) return throwError(TypeErrorKind.CANNOT_DELETE_PROPERTY)
        properties.remove(key)
        return empty
    }
    @EsSpec("[[OwnPropertyKeys]]")
    fun _ownPropertyKeys() =
        properties.keys.toList()
    fun ownPropertyEntries() =
        properties.entries.map { it.toPair() }

    @EsSpec("Get")
    fun get(key: PropertyKey) =
        _get(key, this)
    // GetV is implemented as an extension for LanguageType
    @EsSpec("Set")
    fun set(key: PropertyKey, value: LanguageType) =
        _set(key, value, this)
    @EsSpec("CreateDataProperty")
    fun createDataProperty(key: PropertyKey, value: LanguageType) {
        neverAbrupt(createDataPropertyOrThrow(key, value))
    }
    @EsSpec("CreateMethodProperty")
    fun createMethodProperty(key: PropertyKey, value: LanguageType) {
        neverAbrupt(definePropertyOrThrow(key, DataProperty(value, writable=false, enumerable=false)))
    }
    @EsSpec("CreateDataPropertyOrThrow")
    inline fun createDataPropertyOrThrow(key: PropertyKey, value: LanguageType) =
        _defineOwnProperty(key, DataProperty(value))
    @EsSpec("CreateNonEnumerableDataPropertyOrThrow")
    fun createNonEnumerablePropertyOrThrow(key: PropertyKey, value: LanguageType) =
        definePropertyOrThrow(key, DataProperty(value, enumerable=false))
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
    private inline fun <R> transformOwnEnumerableStringPropertyKeys(transform: (StringType) -> R) =
        ListType(
            ownPropertyEntries().flatMap { (key, desc) ->
                if (key is StringType && desc.enumerable) listOf(transform(key))
                else emptyList()
            }
        )
    @EsSpec("EnumerableOwnProperties") // kind: key
    fun ownEnumerableStringPropertyKeys() =
        transformOwnEnumerableStringPropertyKeys { it }
    @EsSpec("EnumerableOwnProperties") // kind: value
    fun ownEnumerableStringPropertyKeyValues(): MaybeAbrupt<ListType<LanguageType>> {
        return Completion.WideNormal(
            transformOwnEnumerableStringPropertyKeys { key ->
                returnIfAbrupt(get(key)) { return it }
            }
        )
    }
    @EsSpec("EnumerableOwnProperties") // kind: key+value
    fun ownEnumerableStringPropertyKeyEntries(): MaybeAbrupt<ListType<ArrayType>> {
        return Completion.WideNormal(
            transformOwnEnumerableStringPropertyKeys { key ->
                val value = returnIfAbrupt(get(key)) { return it }
                ArrayType.from(listOf(key, value))
            }
        )
    }

    companion object {
        @EsSpec("MakeBasicObject")
        fun createBasic() =
            // TODO: fix if needed
            ObjectType(lazy { null })
        /**
         * Returns an Object that `[[Prototype]]` is set to `%Object.prototype%`.
         */
        fun createNormal(): ObjectType =
            ObjectType()
        @EsSpec("OrdinaryObjectCreate")
        fun create(prototype: PrototypeObjectType?) =
            ObjectType(lazy { prototype })
    }
}
