package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class TryNode(
    val tryBody: BlockNode,
    val catch: CatchNode?,
    val finallyBody: BlockNode?,
    startRange: Range,
): StatementNode, NonAtomicNode {
    override val childNodes get() = listOf(tryBody, catch, finallyBody)
    override val range = startRange..(finallyBody ?: catch!!.body).range
    override fun toString() =
        stringifyLikeDataClass(::tryBody, ::catch, ::finallyBody, ::range)
    override fun evaluate() = lazyFlow {
        val tryRes = yieldAll(tryBody.evaluate())
        val catchRes = catch?.run {
            if (tryRes is Completion.Throw) yieldAll(evaluateCatch(tryRes.value))
            else null
        }
        val finallyRes = finallyBody?.evaluate()
            ?.let { yieldAll(it) }
            ?.takeIf { it is Completion.Abrupt } // its result will be ignored unless it is an abrupt completion
        updateEmpty(finallyRes ?: catchRes ?: tryRes, NullType)
    }
    @EsSpec("CatchClauseEvaluation")
    private fun evaluateCatch(thrown: LanguageType) = lazyFlow f@ {
        requireNotNull(catch)
        val oldEnv = runningExecutionContext.lexicalEnv
        if (catch.binding != null) {
            val catchEnv = DeclarativeEnvironment(oldEnv)
            for (name in catch.binding.boundStringNames()) catchEnv.createMutableBinding(name)
            runningExecutionContext.lexicalEnv = catchEnv
            yieldAll(catch.binding.initializeBy(thrown, catchEnv))
                .orReturn {
                    runningExecutionContext.lexicalEnv = oldEnv
                    return@f it
                }
        }
        val res = yieldAll(catch.body.evaluate())
        runningExecutionContext.lexicalEnv = oldEnv
        res
    }
}
