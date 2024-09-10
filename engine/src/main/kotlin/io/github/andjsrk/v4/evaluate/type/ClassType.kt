package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.orReturnThrow

sealed class ClassType(
    val name: PropertyKey?,
    val parentInstancePrototype: PrototypeObjectType?,
    staticProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    open val constructor: FunctionType,
): ObjectType by ObjectType.Impl(properties = staticProperties) {
    val privateInstanceMethods = mutableMapOf<PrivateName, PrivateProperty>()
    val instanceFields = mutableMapOf<PropertyKey, ClassFieldDefinition>()
    val instancePrototype = ClassAssociatedPrototypeObjectType(this, instancePrototypeProperties)
    init {
        definePropertyOrThrow("instancePrototype".languageValue, DataProperty.sealed(instancePrototype))
    }
    /**
     * Calls the constructor on [thisArg].
     * Note that the method returns [empty] if the constructor did not throw anything,
     * since construction step ignores return value of the constructor.
     * If you want to create an instance in ordinary way, use [new] rather than this method.
     *
     * @see new
     */
    @EsSpec("[[Construct]]")
    abstract fun construct(thisArg: LanguageType, args: List<LanguageType>): MaybeThrow<Nothing?>
    /**
     * Returns a new instance of the class, that is initialized with the constructor.
     */
    fun new(args: List<LanguageType>): MaybeThrow<ObjectType> {
        val instance =
            createNearestBuiltinClassInstance()?.apply {
                prototype = instancePrototype
            }
                ?: ObjectType.Impl(instancePrototype)
        construct(instance, args)
            .orReturnThrow { return it }
        return instance.toNormal()
    }
    /**
     * We need to lift an instance if there is a parent that is a built-in class that uses an internal property
     * because the implementation uses 'field of special class' way instead of 'internal property' way
     *
     * @see construct
     */
    abstract fun createNearestBuiltinClassInstance(): ObjectType?
    fun initializeInstanceElements(instance: ObjectType): EmptyOrThrow {
        privateInstanceMethods.forEach { (_, prop) ->
            instance.addPrivateMethodOrAccessor(prop)
                .orReturnThrow { return it }
        }
        instanceFields.forEach { (_, field) ->
            instance.defineField(field)
                .orReturnThrow { return it }
        }
        return empty
    }
}
