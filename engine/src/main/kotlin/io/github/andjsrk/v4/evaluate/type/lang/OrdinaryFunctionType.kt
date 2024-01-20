package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*

@EsSpec("function object")
@EsSpec("OrdinaryFunctionCreate")
class OrdinaryFunctionType(
    name: PropertyKey?,
    val parameters: UniqueFormalParametersNode,
    val body: ConciseBodyNode,
    override val env: DeclarativeEnvironment,
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
    override val isMethod = thisMode == ThisMode.METHOD
    override fun call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyOrAbrupt {
        val calleeContext = prepareForOrdinaryCall()
        bindThisInCall(calleeContext, thisArg)
        val res = evaluateBody(args).unwrap()
        executionContextStack.removeTop()
        if (res is Completion.Return) return res.value.toNormal()
        res.orReturn { return it }
        // if the function returned nothing, return `null`
        return normalNull
    }
    @EsSpec("PrepareForOrdinaryCall")
    private fun prepareForOrdinaryCall(): ExecutionContext {
        val calleeContext = ExecutionContext(realm, FunctionEnvironment.from(this), this, module=module)
        executionContextStack.addTop(calleeContext)
        return calleeContext
    }
    @EsSpec("OrdinaryCallBindThis")
    fun bindThisInCall(calleeContext: ExecutionContext, thisArg: LanguageType?) {
        if (!isMethod) return
        val localEnv = calleeContext.lexicalEnvNotNull
        require(localEnv is FunctionEnvironment)
        localEnv.bindThisValue(thisArg)
    }
    @EsSpec("EvaluateBody")
    @EsSpec("EvaluateConciseBody")
    @EsSpec("EvaluateAsyncConciseBody")
    @EsSpec("EvaluateFunctionBody")
    @EsSpec("EvaluateAsyncFunctionBody")
    @EsSpec("EvaluateGeneratorBody")
    @EsSpec("EvaluateAsyncGeneratorBody")
    fun evaluateBody(args: List<LanguageType>) = lazyFlow f@ {
        fun evaluateBodyFlexibly(body: ConciseBodyNode) =
            if (isMethod) evaluateStatements(body as BlockNode)
            else evaluateConciseBody(body)

        val instantiationRes = instantiateFunctionDeclaration(this@OrdinaryFunctionType, args)

        when {
            isAsync && isGenerator -> TODO()
            isAsync -> {
                val capability = PromiseType.Capability.new()
                when (instantiationRes) {
                    is Completion.Normal -> capability.startAsyncFunction(evaluateBodyFlexibly(body))
                    is Completion.Throw ->
                        capability.reject.call(null, listOf(instantiationRes.value))
                            .unwrap()
                    else -> neverHappens()
                }
                Completion.Return(capability.promise)
            }
            isGenerator -> {
                instantiationRes.orReturn { return@f it }
                val generator = SyncGeneratorType()
                generator.start(evaluateBodyFlexibly(body))
                Completion.Return(generator)
            }
            else -> {
                instantiationRes.orReturn { return@f it }
                yieldAll(evaluateBodyFlexibly(body))
            }
        }
    }
}
