package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

sealed interface IterationStatementNode: StatementNode, NonAtomicNode {
    val body: StatementNode
    @EsSpec("LabelledEvaluation")
    override fun evaluate() =
        EvalFlow {
            val res = yieldAll(evaluateLoop())
            if (res is Completion.Break) `return`(res.value?.toNormal() ?: `null`)
            `return`(res)
        }
    @EsSpec("LoopEvaluation")
    fun evaluateLoop(): EvalFlow<LanguageType?>
}
