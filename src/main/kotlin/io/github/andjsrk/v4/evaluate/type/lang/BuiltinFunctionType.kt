package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt

typealias BuiltinFunctionBehavior = (thisArg: LanguageType?, args: List<LanguageType>) -> NonEmptyNormalOrAbrupt

@EsSpec("CreateBuiltinFunction")
class BuiltinFunctionType(
    name: PropertyKey? = null,
    requiredParameterCount: UInt = 0u,
    val behavior: BuiltinFunctionBehavior,
): FunctionType(name, requiredParameterCount, runningExecutionContext.lexicalEnvironment) {
    constructor(
        name: String,
        requiredParameterCount: UInt = 0u,
        behavior: BuiltinFunctionBehavior,
    ): this(name.languageValue, requiredParameterCount, behavior)
    override val isArrow = false
    override fun _call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyNormalOrAbrupt {
        val calleeContext = ExecutionContext(FunctionEnvironment.from(this, thisArg), realm, this)
        executionContextStack.push(calleeContext)
        val res = behavior(thisArg, args)
        executionContextStack.pop()
        return res
    }
}

private typealias BuiltinMethodBehavior = (thisArg: LanguageType, args: List<LanguageType>) -> NonEmptyNormalOrAbrupt
internal inline fun builtinMethod(
    name: PropertyKey,
    requiredParameterCount: UInt = 0u,
    crossinline behavior: BuiltinMethodBehavior,
) =
    BuiltinFunctionType(name, requiredParameterCount) fn@ { thisArg, args ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        behavior(thisArg, args)
    }
internal inline fun builtinMethod(
    name: String,
    requiredParameterCount: UInt = 0u,
    crossinline behavior: BuiltinMethodBehavior,
) =
    BuiltinFunctionType(name, requiredParameterCount) fn@ { thisArg, args ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        behavior(thisArg, args)
    }
