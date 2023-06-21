package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.NullType

sealed interface IterationStatementNode: StatementNode, NonAtomicNode {
    val body: StatementNode
    @EsSpec("LabelledEvaluation")
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val res = evaluateLoop()
        if (res is Completion.Break) return Completion.Normal(res.value ?: NullType)
        return res
    }
    @EsSpec("LoopEvaluation")
    fun evaluateLoop(): NonEmptyNormalOrAbrupt
}
