package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.MethodNode

/**
 * Note that methods which its name start with underscore means it is an internal method in ES specification.
 *
 * @param lazyPrototype to avoid some recursions, its type is [Lazy].
 */
open class ObjectType(
    lazyPrototype: Lazy<ObjectType?> = lazy { Object.instancePrototype },
    val properties: MutableMap<PropertyKey, Property> = mutableMapOf(),
): LanguageType {
    val privateElements = mutableMapOf<PrivateName, PrivateProperty>()
    @EsSpec("OrdinaryObjectCreate")
    constructor(prototype: ObjectType?): this(lazy { prototype })
    var prototype by MutableLazy.from(lazyPrototype)
    var extensible = true

    @EsSpec("[[SetPrototypeOf]]")
    fun _setPrototype(prototype: ObjectType?): WasSuccessful {
        val curr = this.prototype
        if (!extensible) return false
        var proto = prototype
        while (true) {
            if (proto == null) break
            else if (curr != null && sameValue(curr, proto)) return false
            else proto = proto.prototype
        }
        this.prototype = prototype
        return true
    }
    @EsSpec("[[GetOwnProperty]]")
    open fun _getOwnProperty(key: PropertyKey): MaybeThrow<Property?> =
        properties[key].toWideNormal()
    @EsSpec("[[DefineOwnProperty]]")
    fun _defineOwnProperty(key: PropertyKey, descriptor: Property): EmptyOrThrow {
        val current = _getOwnProperty(key)
            .orReturnThrow { return it }
        return _applyPropertyDescriptor(key, descriptor, current)
    }
    fun _throwIfNotCompatiblePropertyDescriptor(current: Property?, key: PropertyKey): EmptyOrThrow {
        when {
            current == null ->
                if (!extensible) return throwError(TypeErrorKind.OBJECT_NOT_EXTENSIBLE, key.string())
            current.not { configurable } ->
                return throwError(TypeErrorKind.CANNOT_REDEFINE, key.string())
        }
        return empty
    }
    fun _applyPropertyDescriptor(key: PropertyKey, descriptor: Property, current: Property?): EmptyOrThrow {
        _throwIfNotCompatiblePropertyDescriptor(current, key)
            .orReturnThrow { return it }

        properties[key] = descriptor.clone()
        return empty
    }
    @EsSpec("[[HasProperty]]")
    open fun _hasProperty(key: PropertyKey): MaybeThrow<BooleanType> {
        val hasOwn = hasOwnProperty(key)
            .orReturnThrow { return it }
        if (hasOwn.nativeValue) return hasOwn.toNormal()
        val prototypeHas = prototype?._hasProperty(key)
            ?.orReturnThrow { return it }
        return (prototypeHas ?: BooleanType.FALSE)
            .toNormal()
    }
    @EsSpec("[[Get]]")
    open fun _get(key: PropertyKey, receiver: LanguageType): NonEmptyOrThrow {
        val desc = _getOwnProperty(key)
            .orReturnThrow { return it }
            ?: run {
                val proto = prototype ?: return normalNull
                return proto._get(key, receiver)
            }
        return desc.getValue(receiver, key)
    }
    @EsSpec("[[Set]]")
    open fun _set(key: PropertyKey, value: LanguageType, receiver: LanguageType): MaybeThrow<BooleanType?> {
        when (
            val ownDesc = _getOwnProperty(key)
                .orReturnThrow { return it }
        ) {
            null -> {
                val parent = prototype
                if (parent != null) parent._set(key, value, receiver)
                else properties[key] = DataProperty(value)
            }
            is DataProperty -> {
                if (ownDesc.not { writable }) return throwError(TypeErrorKind.CANNOT_ASSIGN_TO_READ_ONLY_PROPERTY, key.string())
                require(receiver is ObjectType)
                val existingDesc = receiver._getOwnProperty(key)
                    .orReturnThrow { return it }
                if (existingDesc == null) createDataProperty(key, value)
                else {
                    if (existingDesc is AccessorProperty) return BooleanType.FALSE.toNormal()
                    require(existingDesc is DataProperty)
                    if (existingDesc.not { writable }) return throwError(TypeErrorKind.CANNOT_ASSIGN_TO_READ_ONLY_PROPERTY, key.string())
                    receiver._defineOwnProperty(key, existingDesc.copy(value = value))
                }
            }
            is AccessorProperty -> {
                val setter = ownDesc.set ?: return throwError(TypeErrorKind.NO_SETTER, key.string())
                setter.call(receiver, listOf(value))
                    .orReturnThrow { return it }
            }
        }
        return empty
    }
    @EsSpec("[[Delete]]")
    open fun _delete(key: PropertyKey): EmptyOrThrow {
        val desc = _getOwnProperty(key)
            .orReturnThrow { return it }
            ?: return empty
        if (desc.not { configurable }) return throwError(TypeErrorKind.CANNOT_DELETE_PROPERTY)
        properties.remove(key)
        return empty
    }
    @EsSpec("[[OwnPropertyKeys]]")
    fun _ownPropertyKeys() =
        properties.keys.filterIsInstance<LanguageTypePropertyKey>()
    fun ownPropertyEntries() =
        properties.entries.flatMap { entry ->
            if (entry.key is LanguageTypePropertyKey) listOf(entry.toPair())
            else emptyList()
        }

    @EsSpec("Get")
    inline fun get(key: PropertyKey) =
        _get(key, this)
    // GetV is implemented as an extension for LanguageType
    @EsSpec("Set")
    fun set(key: PropertyKey, value: LanguageType) =
        _set(key, value, this)
    @EsSpec("CreateDataProperty")
    fun createDataProperty(key: PropertyKey, value: LanguageType) {
        createDataPropertyOrThrow(key, value)
            .unwrap()
    }
    @EsSpec("CreateDataPropertyOrThrow")
    inline fun createDataPropertyOrThrow(key: PropertyKey, value: LanguageType) =
        _defineOwnProperty(key, DataProperty(value))
    @EsSpec("CreateNonEnumerableDataPropertyOrThrow")
    fun createNonEnumerableDataPropertyOrThrow(key: PropertyKey, value: LanguageType) =
        definePropertyOrThrow(key, DataProperty(value, enumerable = false))
    @EsSpec("DefinePropertyOrThrow")
    inline fun definePropertyOrThrow(key: PropertyKey, descriptor: Property) =
        _defineOwnProperty(key, descriptor)
    @EsSpec("CreateMethodProperty")
    fun createMethodProperty(key: PropertyKey, value: LanguageType) {
        definePropertyOrThrow(key, DataProperty(value, enumerable = false))
            .unwrap()
    }
    @EsSpec("DeletePropertyOrThrow")
    inline fun deletePropertyOrThrow(key: PropertyKey) =
        _delete(key)
    // GetMethod is implemented as an extension for LanguageType
    @EsSpec("HasProperty")
    inline fun hasProperty(key: PropertyKey) =
        _hasProperty(key)
    @EsSpec("HasOwnProperty")
    fun hasOwnProperty(key: PropertyKey): MaybeThrow<BooleanType> {
        val desc = _getOwnProperty(key)
            .orReturnThrow { return it }
        return (desc != null)
            .languageValue
            .toNormal()
    }
    @EsSpec("DefineField")
    fun defineField(field: ClassFieldDefinition): EmptyOrThrow {
        val initialValue = field.initializer?.call(this)
            ?.orReturnThrow { return it }
            ?: NullType
        return (
            if (field.name is PrivateName) addPrivateField(field.name, initialValue)
            else createDataPropertyOrThrow(field.name, initialValue)
        )
    }
    @EsSpec("DefineMethod")
    fun defineMethod(methodNode: MethodNode) = lazyFlow f@ {
        val name = yieldAll(methodNode.name.toLanguageTypePropertyKey())
            .orReturn { return@f it }
        OrdinaryFunctionType(name, methodNode.parameters, methodNode.body, ThisMode.METHOD)
            .apply {
                homeObject = this@ObjectType
            }
            .toNormal()
    }
    @EsSpec("DefineMethodProperty")
    fun defineMethodProperty(key: PropertyKey, method: FunctionType, enumerable: Boolean): MaybeAbrupt<PrivateProperty?> {
        if (key is PrivateName) return PrivateProperty(key, DataProperty(method, false, enumerable = enumerable)).toWideNormal()
        definePropertyOrThrow(key, DataProperty(method, true, enumerable = enumerable))
            .unwrap()
        return empty
    }
    @EsSpec("PrivateFieldAdd")
    fun addPrivateField(key: PrivateName, value: LanguageType): EmptyOrThrow {
        if (key in privateElements) return throwError(SyntaxErrorKind.INVALID_PRIVATE_MEMBER_REINITIALIZATION, key.string())
        privateElements[key] = PrivateProperty(key, DataProperty(value, enumerable = false, configurable = false))
        return empty
    }
    @EsSpec("PrivateMethodOrAccessorAdd")
    fun addPrivateMethodOrAccessor(prop: PrivateProperty): EmptyOrThrow {
        if (prop.key in privateElements) return throwError(SyntaxErrorKind.INVALID_PRIVATE_MEMBER_REINITIALIZATION, prop.key.string())
        privateElements[prop.key] = prop
        return empty
    }
    @EsSpec("SetIntegrityLevel")
    fun setImmutabilityLevel(level: ObjectImmutabilityLevel): EmptyOrThrow {
        val keys = _ownPropertyKeys()
        for (key in keys) {
            val desc = _getOwnProperty(key)
                .orReturnThrow { return it }!!
                .clone()
                .apply {
                    configurable = false
                    if (level == ObjectImmutabilityLevel.FROZEN && this is DataProperty) writable = false
                }
            definePropertyOrThrow(key, desc)
                .orReturnThrow { return it }
        }
        return empty
    }
    @EsSpec("TestIntegrityLevel")
    fun satisfiesImmutabilityLevel(level: ObjectImmutabilityLevel): MaybeThrow<GeneralSpecValue<Boolean>> {
        if (extensible) return false.toGeneralWideNormal()
        for (key in _ownPropertyKeys()) {
            val desc = _getOwnProperty(key)
                .orReturnThrow { return it }!!
            if (desc.configurable) return false.toGeneralWideNormal()
            if (level == ObjectImmutabilityLevel.FROZEN && desc is DataProperty && desc.writable) {
                return false.toGeneralWideNormal()
            }
        }
        return true.toGeneralWideNormal()
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
    fun ownEnumerableStringPropertyKeyValues(): MaybeThrow<ListType<LanguageType>> {
        return transformOwnEnumerableStringPropertyKeys { key ->
            get(key)
                .orReturnThrow { return it }
        }
            .toWideNormal()
    }
    @EsSpec("EnumerableOwnProperties") // kind: key+value
    fun ownEnumerableStringKeyEntries(): MaybeThrow<ListType<ArrayType>> {
        return transformOwnEnumerableStringPropertyKeys { key ->
            val value = get(key)
                .orReturnThrow { return it }
            ImmutableArrayType.from(listOf(key, value))
        }
            .toWideNormal()
    }

    override fun toString() = display()

    companion object {
        /**
         * Returns an Object that `[[Prototype]]` is set to `%Object.prototype%`.
         */
        fun createNormal(properties: MutableMap<PropertyKey, Property> = mutableMapOf()): ObjectType =
            ObjectType(properties = properties)
    }
}
