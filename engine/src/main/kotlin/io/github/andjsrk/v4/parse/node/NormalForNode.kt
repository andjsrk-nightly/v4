package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.*

class NormalForNode(
    val init: NormalLexicalDeclarationNode?,
    val test: ExpressionNode?,
    val update: ExpressionNode?,
    override val body: StatementNode,
    startRange: Range,
): ForNode {
    override val childNodes get() = listOf(init, test, update, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::init, ::test, ::update, ::body, ::range)
    override fun evaluateLoop() = lazyFlow f@ {
        var bindingNames = emptyList<String>()
        val oldEnv = runningExecutionContext.lexicalEnvNotNull
        if (init != null) {
            val loopEnv = DeclarativeEnvironment(oldEnv)
            val names = init.boundStringNames()
            init.instantiateIn(loopEnv, names)
            runningExecutionContext.lexicalEnvNotNull = loopEnv
            val initRes = yieldAll(init.evaluate())
            if (initRes is Completion.Abrupt) {
                runningExecutionContext.lexicalEnvNotNull = oldEnv
                return@f initRes
            }
            if (init.isConstant) bindingNames = names
        }
        val body = yieldAll(evaluateBody(bindingNames))
        runningExecutionContext.lexicalEnvNotNull = oldEnv
        body
    }
    @EsSpec("ForBodyEvaluation")
    private fun evaluateBody(bindingNames: List<String>) = lazyFlow f@ {
        var res: LanguageType = NullType
        coverBindingsPerIteration(bindingNames)
        while (true) {
            if (test != null) {
                val testValue = yieldAll(test.evaluateValue())
                    .orReturn { return@f it }
                    .requireToBe<BooleanType> { return@f it }
                if (!testValue.nativeValue) return@f res.toNormal()
            }
            res = yieldAll(body.evaluate())
                .returnIfShouldNotContinue(res) { return@f it }
            coverBindingsPerIteration(bindingNames)
            update?.evaluateValue()
                ?.let { yieldAll(it) }
                ?.orReturn { return@f it }
        }
        @CompilerFalsePositive
        neverHappens()
    }
}

@EsSpec("CreatePerIterationEnvironment")
private fun coverBindingsPerIteration(bindingNames: List<String>) {
    if (bindingNames.isEmpty()) return

    val lastIterationEnv = runningExecutionContext.lexicalEnvNotNull
    val outer = lastIterationEnv.outer
    requireNotNull(outer)
    val currIterationEnv = DeclarativeEnvironment(outer)
    for (name in bindingNames) {
        currIterationEnv.createMutableBinding(name).unwrap()
        val lastValue = lastIterationEnv.getBindingValue(name)
            .unwrap()
        currIterationEnv.initializeBinding(name, lastValue).unwrap()
    }
    runningExecutionContext.lexicalEnvNotNull = currIterationEnv
}
