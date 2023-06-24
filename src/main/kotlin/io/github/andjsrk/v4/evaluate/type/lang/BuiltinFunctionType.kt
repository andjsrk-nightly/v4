package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt

typealias BuiltinFunctionBehavior = (thisArg: LanguageType, args: List<LanguageType>) -> NonEmptyNormalOrAbrupt

@EsSpec("CreateBuiltinFunction")
class BuiltinFunctionType(
    name: PropertyKey? = null,
    requiredParameterCount: UInt,
    val behavior: BuiltinFunctionBehavior,
): FunctionType(name, requiredParameterCount, runningExecutionContext.lexicalEnvironment) {
    override val isArrow = false
    override fun _call(thisArg: LanguageType, args: List<LanguageType>): NonEmptyNormalOrAbrupt {
        val calleeContext = ExecutionContext(FunctionEnvironment.from(this, thisArg), realm, this)
        executionContextStack.push(calleeContext)
        val res = behavior(thisArg, args)
        executionContextStack.pop()
        return res
    }
}
