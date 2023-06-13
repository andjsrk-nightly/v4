package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

sealed interface IterationStatementNode: StatementNode, NonAtomicNode {
    val body: StatementNode
    @EsSpec("LabelledEvaluation")
    override fun evaluate(): Completion {
        val res = evaluateLoop()
        if (res.type == Completion.Type.BREAK) return Completion.normal(res.languageValue ?: NullType)
        return res
    }
    @EsSpec("LoopEvaluation")
    fun evaluateLoop(): Completion
}
