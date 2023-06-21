package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.node.ConciseBodyNode
import io.github.andjsrk.v4.parse.node.UniqueFormalParametersNode

@EsSpec("function object")
@EsSpec("OrdinaryFunctionCreate")
class FunctionType(
    val parameters: UniqueFormalParametersNode,
    val body: ConciseBodyNode,
    env: DeclarativeEnvironment,
    val mode: ThisMode,
    name: PropertyKey? = null,
): AbstractFunctionType(name, parameters.requiredParameterCount, env) {
    override fun _call(thisArg: LanguageType, args: List<LanguageType>): Completion {
        val callerContext = runningExecutionContext
        val calleeContext = prepareForOrdinaryCall()
        TODO()
    }
}
