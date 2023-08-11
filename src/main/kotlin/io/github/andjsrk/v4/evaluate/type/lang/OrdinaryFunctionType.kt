package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.ConciseBodyNode
import io.github.andjsrk.v4.parse.node.UniqueFormalParametersNode

@EsSpec("function object")
@EsSpec("OrdinaryFunctionCreate")
class OrdinaryFunctionType(
    name: PropertyKey?,
    val parameters: UniqueFormalParametersNode,
    val body: ConciseBodyNode,
    env: DeclarativeEnvironment,
    val thisMode: ThisMode,
    val isAsync: Boolean = false,
    val isGenerator: Boolean = false,
): FunctionType(
    name,
    parameters.requiredParameterCount,
    env,
    lazy {
        when {
            isAsync && isGenerator -> AsyncGeneratorFunction
            isAsync -> AsyncFunction
            isGenerator -> GeneratorFunction
            else -> Function
        }
            .instancePrototype
    },
) {
    override val isArrow = thisMode == ThisMode.ARROW
    override fun _call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyNormalOrAbrupt {
        val calleeContext = prepareForOrdinaryCall()
        bindThisInCall(calleeContext, thisArg)
        val res = evaluateBody(args)
        executionContextStack.removeTop()
        if (res is Completion.Return) return res.value.toNormal()
        res.returnIfAbrupt { return it }
        // if the function returned nothing, return `null`
        return `null`
    }
    @EsSpec("PrepareForOrdinaryCall")
    internal fun prepareForOrdinaryCall(): ExecutionContext {
        val calleeContext = ExecutionContext(FunctionEnvironment.from(this), realm, function=this)
        executionContextStack.addTop(calleeContext)
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
        if (this.isArrow) evaluateConciseBody(args)
        else evaluateMethodBody(args)
    @EsSpec("EvaluateConciseBody")
    @EsSpec("EvaluateFunctionBody")
    @EsSpec("EvaluateGeneratorBody")
    fun evaluateConciseBody(args: List<LanguageType>): NormalOrAbrupt {
        return when {
            isAsync && isGenerator -> TODO()
            isAsync -> TODO()
            isGenerator ->
                EvalFlow {
                    instantiateFunctionDeclaration(this@OrdinaryFunctionType, args)
                        .returnIfAbrupt(this) { return@EvalFlow }
                    val generator = SyncGeneratorType()
                    generator.start { evaluateConciseBody(body) }
                    `return`(Completion.Return(generator))
                }
                    .toSyncGenerator()
                    .toNormal()
            else -> {
                instantiateFunctionDeclaration(this@OrdinaryFunctionType, args)
                    .takeReturnValueNoYields()
                    ?.returnIfAbrupt { return it }
                    ?: empty
                return evaluateConciseBody(body)
                    .takeReturnValueNoYields()
                    ?: empty
            }
        }
    }
    fun evaluateMethodBody(args: List<LanguageType>): NormalOrAbrupt {
        TODO()
    }
}
