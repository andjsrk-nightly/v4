package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
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
    override fun evaluateLoop(): NonEmptyNormalOrAbrupt {
        var bindingNames = emptyList<String>()
        val oldEnv = runningExecutionContext.lexicalEnvironment
        if (init != null) {
            val loopEnv = DeclarativeEnvironment(oldEnv)
            val names = init.boundStringNames()
            init.instantiateIn(loopEnv, names)
            runningExecutionContext.lexicalEnvironment = loopEnv
            val initRes = init.evaluate()
            if (initRes is Completion.Abrupt) {
                runningExecutionContext.lexicalEnvironment = oldEnv
                return initRes
            }
            if (init.isConstant) bindingNames = names
        }
        val body = evaluateBody(bindingNames)
        runningExecutionContext.lexicalEnvironment = oldEnv
        return body
    }
    @EsSpec("ForBodyEvaluation")
    private fun evaluateBody(bindingNames: List<String>): NonEmptyNormalOrAbrupt {
        var res: LanguageType = NullType
        runningExecutionContext.lexicalEnvironment.coverBindingsPerIteration(bindingNames)
        while (true) {
            if (test != null) {
                val testValue = test.evaluateValue().orReturn { return it }
                    .requireToBe<BooleanType> { return it }
                if (!testValue.value) return res.toNormal()
            }
            res = body.evaluate().returnIfShouldNotContinue(res) { return it }
            runningExecutionContext.lexicalEnvironment.coverBindingsPerIteration(bindingNames)
            update?.evaluateValue()?.orReturn { return it }
        }
    }
}

@EsSpec("CreatePerIterationEnvironment")
private fun DeclarativeEnvironment.coverBindingsPerIteration(bindingNames: List<String>) {
    if (bindingNames.isEmpty()) return
    val lastIterationEnv = runningExecutionContext.lexicalEnvironment
    val outer = lastIterationEnv.outer
    requireNotNull(outer)
    val currIterationEnv = DeclarativeEnvironment(outer)
    for (name in bindingNames) {
        currIterationEnv.createNonConfigurableMutableBinding(name)
        val lastValue = lastIterationEnv.getValue(name)
            .unwrap()
        currIterationEnv.initializeBinding(name, lastValue)
    }
    runningExecutionContext.lexicalEnvironment = currIterationEnv
}
