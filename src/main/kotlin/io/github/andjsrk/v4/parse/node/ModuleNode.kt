package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateStatements
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ModuleNode(override val elements: List<StatementNode>): StatementListNode {
    override val childNodes = elements
    override val range = Range(0, elements.lastOrNull()?.range?.end ?: 0)
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): NormalOrAbrupt {
        val res = evaluateStatements()
        if (res is Completion.WideNormal<*> && res.value == null) return Completion.Normal.`null`
        return res
    }
}
