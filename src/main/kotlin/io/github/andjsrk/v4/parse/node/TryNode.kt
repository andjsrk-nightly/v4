package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
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
    override fun evaluate(): Completion {
        val tryRes = tryBody.evaluate()
        val didTryBlockThrow = tryRes.type == Completion.Type.THROW
        val catchRes = catch?.run {
            didTryBlockThrow.thenTake {
                evaluateCatch(tryRes.languageValue!!)
            }
        }
        val finallyRes = finallyBody?.evaluate()?.takeIf { it.isAbrupt } // its result will be ignored unless it is an abrupt completion
        return updateEmpty(finallyRes ?: catchRes ?: tryRes, NullType)
    }
    @EsSpec("CatchClauseEvaluation")
    private fun evaluateCatch(thrown: LanguageType): Completion {
        requireNotNull(catch)
        val oldEnv = runningExecutionContext.lexicalEnvironment
        if (catch.binding != null) {
            val catchEnv = DeclarativeEnvironment(oldEnv)
            for (name in catch.binding.boundStringNames()) catchEnv.createMutableBinding(name)
            runningExecutionContext.lexicalEnvironment = catchEnv
            val bindingRes = catch.binding.initialize(thrown, catchEnv)
            if (bindingRes.isAbrupt) {
                runningExecutionContext.lexicalEnvironment = oldEnv
                return bindingRes
            }
        }
        val res = catch.body.evaluate()
        runningExecutionContext.lexicalEnvironment = oldEnv
        return res
    }
}
