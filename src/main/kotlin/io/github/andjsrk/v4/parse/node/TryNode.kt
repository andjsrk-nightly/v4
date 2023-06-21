package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
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
    override fun evaluate(): NormalOrAbrupt {
        val tryRes = tryBody.evaluate()
        val catchRes = catch?.run {
            if (tryRes is Completion.Throw) evaluateCatch(tryRes.value)
            else null
        }
        val finallyRes = finallyBody?.evaluate()?.takeIf { it is Completion.Abrupt } // its result will be ignored unless it is an abrupt completion
        return updateEmpty(finallyRes ?: catchRes ?: tryRes, NullType)
    }
    @EsSpec("CatchClauseEvaluation")
    private fun evaluateCatch(thrown: LanguageType): NormalOrAbrupt {
        requireNotNull(catch)
        val oldEnv = runningExecutionContext.lexicalEnvironment
        if (catch.binding != null) {
            val catchEnv = DeclarativeEnvironment(oldEnv)
            for (name in catch.binding.boundStringNames()) catchEnv.createNonConfigurableMutableBinding(name)
            runningExecutionContext.lexicalEnvironment = catchEnv
            val bindingRes = catch.binding.initialize(thrown, catchEnv)
            if (bindingRes is Completion.Abrupt) {
                runningExecutionContext.lexicalEnvironment = oldEnv
                return bindingRes
            }
        }
        val res = catch.body.evaluate()
        runningExecutionContext.lexicalEnvironment = oldEnv
        return res
    }
}
