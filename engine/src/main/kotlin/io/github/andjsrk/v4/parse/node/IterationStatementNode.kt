package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.SimpleLazyFlow
import io.github.andjsrk.v4.evaluate.lazyFlow
import io.github.andjsrk.v4.evaluate.type.*

sealed interface IterationStatementNode: StatementNode, NonAtomicNode {
    val body: StatementNode
    @EsSpec("LabelledEvaluation")
    override fun evaluate() = lazyFlow {
        val res = yieldAll(evaluateLoop())
        if (res is Completion.Break) res.value.normalizeToNormal()
        else res
    }
    @EsSpec("LoopEvaluation")
    fun evaluateLoop(): SimpleLazyFlow<NonEmptyOrAbrupt>
}
