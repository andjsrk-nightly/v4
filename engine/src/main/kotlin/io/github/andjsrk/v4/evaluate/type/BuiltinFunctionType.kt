package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*

private typealias BuiltinFunctionBehavior = (thisArg: LanguageType?, args: List<LanguageType>) -> NonEmptyOrThrow

@EsSpec("CreateBuiltinFunction")
class BuiltinFunctionType(
    name: PropertyKey? = null,
    requiredParameterCount: UInt = 0u,
    val behavior: BuiltinFunctionBehavior,
): FunctionType(name, requiredParameterCount, runningExecutionContext.lexicalEnv) {
    constructor(
        name: String?,
        requiredParameterCount: UInt = 0u,
        behavior: BuiltinFunctionBehavior,
    ): this(name?.languageValue, requiredParameterCount, behavior)
    override val isMethod = true
    override fun call(thisArg: LanguageType?, args: List<LanguageType>) =
        withTemporalCtx(createContextForCall()) {
            behavior(thisArg, args)
        }
    override fun ordinaryCallEvaluateBody(args: List<LanguageType>): NonEmptyOrThrow =
        behavior(null, args)
}

private typealias BuiltinMethodBehavior = (thisArg: LanguageType, args: List<LanguageType>) -> NonEmptyOrThrow
inline fun method(
    name: PropertyKey?,
    requiredParamCount: UInt = 0u,
    crossinline behavior: BuiltinMethodBehavior,
) =
    BuiltinFunctionType(name, requiredParamCount) fn@ { thisArg, args ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        behavior(thisArg, args)
    }
inline fun method(
    name: String? = null,
    requiredParamCount: UInt = 0u,
    crossinline behavior: BuiltinMethodBehavior,
) =
    method(name?.languageValue, requiredParamCount, behavior)
inline fun functionWithoutThis(
    name: String? = null,
    requiredParamCount: UInt = 0u,
    crossinline behavior: (args: List<LanguageType>) -> NonEmptyOrThrow,
) =
    BuiltinFunctionType(name, requiredParamCount) { _, args ->
        behavior(args)
    }

fun List<LanguageType>.getOptional(index: Int) =
    getOrNull(index)?.normalizeNull()
