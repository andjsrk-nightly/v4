package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ForInNode(
    val declaration: LexicalDeclarationWithoutInitializerNode,
    val target: ExpressionNode,
    override val body: StatementNode,
    startRange: Range,
): ForNode {
    override val childNodes get() = listOf(declaration, target, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::declaration, ::target, ::body, ::range)
    override fun evaluateLoop() = lazyFlow f@ {
        val iter = yieldAll(evaluateHead())
            .orReturn { return@f it }
        yieldAll(evaluateBody(iter))
    }
    private fun evaluateHead() = lazyFlow f@ {
        val oldEnv = runningExecutionContext.lexicalEnvNotNull
        val uninitializedBoundNames = declaration.boundStringNames()
        if (uninitializedBoundNames.isNotEmpty()) {
            val newEnv = DeclarativeEnvironment(oldEnv)
            uninitializedBoundNames.forEach {
                newEnv.createMutableBinding(it)
            }
            runningExecutionContext.lexicalEnvNotNull = newEnv
        }
        val targetValueOrAbrupt = yieldAll(target.evaluateValue())
        runningExecutionContext.lexicalEnvNotNull = oldEnv
        val targetValue = targetValueOrAbrupt
            .orReturn { return@f it }
        IteratorRecord.from(targetValue)
    }
    @EsSpec("ForIn/OfBodyEvaluation")
    private fun evaluateBody(iterRec: IteratorRecord) = lazyFlow f@ {
        val oldEnv = runningExecutionContext.lexicalEnvNotNull
        var res: LanguageType = NullType
        while (true) {
            val nextRes = iterRec.step()
                .orReturn { return@f it }
                ?: return@f res.toNormal()
            val nextValue = nextRes.getValue()
                .orReturn { return@f it }
            val iteratorEnv = DeclarativeEnvironment(oldEnv)
            declaration.instantiateIn(iteratorEnv)
            runningExecutionContext.lexicalEnvNotNull = iteratorEnv
            yieldAll(declaration.binding.initializeBy(nextValue, iteratorEnv))
                .orReturn {
                    runningExecutionContext.lexicalEnvNotNull = oldEnv
                    return@f iterRec.close(it)
                }
            val stmtRes = yieldAll(body.evaluate())
            runningExecutionContext.lexicalEnvNotNull = oldEnv
            if (!continueLoop(stmtRes)) {
                require(stmtRes is Completion.Abrupt)
                val abrupt = updateEmpty(stmtRes, res)
                return@f iterRec.close(abrupt)
            }
            val stmtResValue = stmtRes.value as LanguageType? // we can sure that the completion is either a normal completion or a continue completion
            if (stmtResValue != null) res = stmtResValue
        }
        @CompilerFalsePositive
        neverHappens()
    }
}
