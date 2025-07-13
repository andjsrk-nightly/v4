package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.parse.ConstructorKind

class OrdinaryClassType(
    name: PropertyKey?,
    parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    constructor: FunctionType,
    val constructorKind: ConstructorKind,
): ClassType(name, parent?.instancePrototype, staticProperties, instancePrototypeProperties, constructor) {
    override fun construct(thisArg: LanguageType, args: List<LanguageType>): EmptyOrThrow {
        thisArg.requireToBe<ObjectType> { return it }
        val kind = constructorKind
        val calleeCtx = constructor.createContextForCall()
        val res = withTemporalCtx(calleeCtx) {
            if (kind == ConstructorKind.BASE) {
                if (constructor is OrdinaryFunctionType) {
                    constructor.bindThisInCall(calleeCtx, thisArg)
                }
                initializeInstanceElements(thisArg)
                    .orReturnThrow { return it }
            }
            when (constructor) {
                is OrdinaryFunctionType -> constructor.ordinaryCallEvaluateBody(args)
                is BuiltinFunctionType -> constructor.behavior(thisArg, args) // FIXME: make this abstract
            }
        }
        if (res is Completion.Throw) return res
        return empty
    }
    override fun createNearestBuiltinClassInstance() =
        // returns parent's result since the class is not built-in
        parentInstancePrototype?.ownerClass?.createNearestBuiltinClassInstance()
}
