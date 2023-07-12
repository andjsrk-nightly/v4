package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.ConciseBodyNode
import io.github.andjsrk.v4.parse.node.UniqueFormalParametersNode

@EsSpec("function object")
@EsSpec("OrdinaryFunctionCreate")
class OrdinaryFunctionType(
    val parameters: UniqueFormalParametersNode,
    val body: ConciseBodyNode,
    env: DeclarativeEnvironment,
    val thisMode: ThisMode,
    name: PropertyKey? = null,
): FunctionType(name, parameters.requiredParameterCount, env) {
    override val isArrow = thisMode == ThisMode.ARROW
    override fun _call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyNormalOrAbrupt {
        val calleeContext = prepareForOrdinaryCall()
        bindThisInCall(calleeContext, thisArg)
        val res = evaluateBody(args)
        executionContextStack.pop()
        if (res is Completion.Return) return Completion.Normal(res.value)
        returnIfAbrupt(res) { return it }
        // if the function returned nothing, return `null`
        return Completion.Normal.`null`
    }
    @EsSpec("PrepareForOrdinaryCall")
    internal fun prepareForOrdinaryCall(): ExecutionContext {
        val calleeContext = ExecutionContext(FunctionEnvironment.from(this), realm, function=this)
        executionContextStack.push(calleeContext)
        return calleeContext
    }
    @EsSpec("OrdinaryCallBindThis")
    fun bindThisInCall(calleeContext: ExecutionContext, thisArg: LanguageType?) {
        if (thisMode == ThisMode.ARROW) return
        val localEnv = calleeContext.lexicalEnvironment
        require(localEnv is FunctionEnvironment)
        localEnv.bindThisValue(thisArg)
    }
    @EsSpec("EvaluateBody")
    fun evaluateBody(args: List<LanguageType>): NormalOrAbrupt =
        when {
            this.isArrow -> evaluateConciseBody(args)
            else -> TODO()
        }
    @EsSpec("EvaluateFunctionBody")
    @EsSpec("EvaluateConciseBody")
    fun evaluateConciseBody(args: List<LanguageType>): NormalOrAbrupt {
        returnIfAbrupt(instantiateFunctionDeclaration(this, args)) { return it }
        return body.evaluateAsConciseBody()
    }
}
