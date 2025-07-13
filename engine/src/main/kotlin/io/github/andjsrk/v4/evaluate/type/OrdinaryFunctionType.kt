package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*

@EsSpec("function object")
@EsSpec("OrdinaryFunctionCreate")
class OrdinaryFunctionType(
    name: PropertyKey?,
    val parameters: UniqueFormalParametersNode,
    val body: ConciseBodyNode,
    val thisMode: ThisMode,
    val isAsync: Boolean = false,
    val isGenerator: Boolean = false,
    override val env: DeclarativeEnvironment = runningExecutionContext.lexicalEnvNotNull,
    privateEnv: PrivateEnvironment? = runningExecutionContext.privateEnv,
): FunctionType(
    name,
    parameters.requiredParameterCount,
    env,
    privateEnv,
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
    override val isMethod = thisMode == ThisMode.METHOD
    override fun call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyOrThrow {
        val res = withTemporalCtx(createContextForCall()) {
            bindThisInCall(runningExecutionContext, thisArg)
            evaluateBody(args)
        }
        if (res is Completion.Return) return res.value.toNormal()
        require(res is MaybeThrow<*>)
        res.orReturnThrow { return it }
        // if the function returned nothing, return `null`
        return normalNull
    }
    @EsSpec("OrdinaryCallBindThis")
    fun bindThisInCall(calleeContext: ExecutionContext, thisArg: LanguageType?) {
        if (!isMethod) return
        val localEnv = calleeContext.lexicalEnvNotNull
        require(localEnv is FunctionEnvironment)
        localEnv.bindThisValue(thisArg)
    }
    override fun ordinaryCallEvaluateBody(args: List<LanguageType>): Completion.FromFunctionBody<*> {
        return evaluateBody(args)
    }
    @EsSpec("EvaluateBody")
    @EsSpec("EvaluateConciseBody")
    @EsSpec("EvaluateAsyncConciseBody")
    @EsSpec("EvaluateFunctionBody")
    @EsSpec("EvaluateAsyncFunctionBody")
    @EsSpec("EvaluateGeneratorBody")
    @EsSpec("EvaluateAsyncGeneratorBody")
    fun evaluateBody(args: List<LanguageType>): Completion.FromFunctionBody<*> {
        fun evaluateBodyFlexibly(body: ConciseBodyNode) =
            (
                if (isMethod) evaluateStatements(body as BlockNode).asFromFunctionBody()
                else evaluateConciseBody(body)
            )

        val instantiationRes = instantiateFunctionDeclaration(this@OrdinaryFunctionType, args)

        when {
            isAsync && isGenerator -> {
                instantiationRes.orReturnNonEmpty { return it }
                val generator = AsyncGeneratorType()
                generator.start(evaluateBodyFlexibly(body))
                return Completion.Return(generator)
            }
            isAsync -> {
                val capability = PromiseType.Capability.new()
                when (instantiationRes) {
                    is Completion.Normal -> capability.startAsyncFunction(evaluateBodyFlexibly(body))
                    is Completion.Throw ->
                        capability.reject.callWithSingleArg(instantiationRes.value)
                            .unwrap()
                    else -> neverHappens()
                }
                return Completion.Return(capability.promise)
            }
            isGenerator -> {
                instantiationRes.orReturnNonEmpty { return it }
                val generator = SyncGeneratorType()
                generator.start(evaluateBodyFlexibly(body))
                return Completion.Return(generator)
            }
            else -> {
                instantiationRes.orReturnNonEmpty { return it }
                return evaluateBodyFlexibly(body).unwrap()
            }
        }
    }
}
