package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment

typealias BuiltinFunctionBehavior = (thisArg: LanguageType, args: List<LanguageType>) -> Completion

@EsSpec("CreateBuiltinFunction")
class BuiltinFunctionType(
    name: PropertyKey? = null,
    requiredParameterCount: UInt,
    val behavior: BuiltinFunctionBehavior,
): AbstractFunctionType(name, requiredParameterCount, runningExecutionContext.lexicalEnvironment) {
    override fun _call(thisArg: LanguageType, args: List<LanguageType>): Completion {
        val callerContext = runningExecutionContext
        val calleeContext = ExecutionContext(FunctionEnvironment.from(this, thisArg), realm, this)
        val res = behavior(thisArg, args)
        Evaluator.runningExecutionContext = callerContext
        return res
    }
}
