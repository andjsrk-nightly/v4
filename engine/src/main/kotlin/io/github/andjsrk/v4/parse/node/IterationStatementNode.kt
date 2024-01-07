package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

sealed interface IterationStatementNode: StatementNode, NonAtomicNode {
    val body: StatementNode
    @EsSpec("LabelledEvaluation")
    override fun evaluate(): NonEmptyOrAbrupt {
        val res = evaluateLoop()
        if (res is Completion.Break) return res.value.normalizeToNormal()
        return res
    }
    @EsSpec("LoopEvaluation")
    fun evaluateLoop(): NonEmptyOrAbrupt
}
