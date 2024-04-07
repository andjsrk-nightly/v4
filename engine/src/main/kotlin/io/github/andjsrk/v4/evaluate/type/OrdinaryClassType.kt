package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.withTemporalCtx

class OrdinaryClassType(
    name: PropertyKey?,
    parent: ClassType?,
    staticProperties: MutableMap<PropertyKey, Property>,
    instancePrototypeProperties: MutableMap<PropertyKey, Property> = mutableMapOf(),
    constructor: FunctionType,
): ClassType(name, parent?.instancePrototype, staticProperties, instancePrototypeProperties, constructor) {
    override fun construct(thisArg: LanguageType, args: List<LanguageType>): MaybeThrow<Nothing?> {
        thisArg.requireToBe<ObjectType> { return it }
        val res = withTemporalCtx(constructor.createContextForCall()) {
            constructor.evaluateBody(thisArg, args)
        }
        if (res is Completion.Throw) return res
        return empty
    }
    override fun createNearestBuiltinClassInstance() =
        // returns parent's result since the class is not built-in
        parentInstancePrototype?.ownerClass?.createNearestBuiltinClassInstance()
}
