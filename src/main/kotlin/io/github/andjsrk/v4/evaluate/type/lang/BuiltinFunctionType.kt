package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.NonEmptyOrAbrupt

private typealias BuiltinFunctionBehavior = (thisArg: LanguageType?, args: List<LanguageType>) -> NonEmptyOrAbrupt

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
    override val isMethod = true
    override fun call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyOrAbrupt {
        val calleeContext = ExecutionContext(realm, FunctionEnvironment.from(this, thisArg), this)
        executionContextStack.addTop(calleeContext)
        val res = behavior(thisArg, args)
        executionContextStack.removeTop()
        return res
    }
}

private typealias BuiltinMethodBehavior = (thisArg: LanguageType, args: List<LanguageType>) -> NonEmptyOrAbrupt
internal inline fun method(
    name: PropertyKey,
    requiredParamCount: UInt = 0u,
    crossinline behavior: BuiltinMethodBehavior,
) =
    BuiltinFunctionType(name, requiredParamCount) fn@ { thisArg, args ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        behavior(thisArg, args)
    }
internal inline fun method(
    name: String,
    requiredParamCount: UInt = 0u,
    crossinline behavior: BuiltinMethodBehavior,
) =
    BuiltinFunctionType(name, requiredParamCount) fn@ { thisArg, args ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        behavior(thisArg, args)
    }
internal inline fun functionWithoutThis(
    name: String,
    requiredParamCount: UInt = 0u,
    crossinline behavior: (args: List<LanguageType>) -> NonEmptyOrAbrupt,
) =
    BuiltinFunctionType(name, requiredParamCount) { _, args ->
        behavior(args)
    }

internal fun List<LanguageType>.getOptional(index: Int) =
    getOrNull(index)?.normalizeNull()
