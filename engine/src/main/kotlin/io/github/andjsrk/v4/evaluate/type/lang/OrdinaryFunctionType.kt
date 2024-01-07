package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.*

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
    override val isMethod = thisMode == ThisMode.METHOD
    override fun call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyOrAbrupt {
        val calleeContext = prepareForOrdinaryCall()
        bindThisInCall(calleeContext, thisArg)
        val res = evaluateBody(args)
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
        val localEnv = calleeContext.lexicalEnvironment
        require(localEnv is FunctionEnvironment)
        localEnv.bindThisValue(thisArg)
    }
    @EsSpec("EvaluateBody")
    fun evaluateBody(args: List<LanguageType>): MaybeEmptyOrAbrupt =
        if (isMethod) evaluateMethodBody(args)
        else evaluateConciseBody(args)
    @EsSpec("EvaluateConciseBody")
    @EsSpec("EvaluateAsyncConciseBody")
    fun evaluateConciseBody(args: List<LanguageType>): MaybeEmptyOrAbrupt {
        return when {
            this.isAsync && this.isGenerator -> TODO()
            this.isAsync -> TODO()
            this.isGenerator -> {
                instantiateFunctionDeclaration(this, args)
                val generator = SyncGeneratorType()
                generator.start(sequence {
                    yield(evaluateConciseBody(body))
                })
                Completion.Return(generator)
            }
            else -> {
                instantiateFunctionDeclaration(this, args)
                    .orReturn { return it }
                evaluateConciseBody(body)
            }
        }
    }
    @EsSpec("EvaluateFunctionBody")
    @EsSpec("EvaluateAsyncFunctionBody")
    @EsSpec("EvaluateGeneratorBody")
    @EsSpec("EvaluateAsyncGeneratorBody")
    fun evaluateMethodBody(args: List<LanguageType>): MaybeEmptyOrAbrupt {
        return when {
            isAsync && isGenerator -> TODO()
            isAsync -> TODO()
            isGenerator -> TODO()
            else -> {
                instantiateFunctionDeclaration(this, args)
                    .orReturn { return it }
                evaluateStatements(body as BlockNode)
            }
        }
    }
}
