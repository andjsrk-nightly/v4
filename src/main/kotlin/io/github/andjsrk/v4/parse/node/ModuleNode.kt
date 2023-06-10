package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.evaluate.updateEmpty
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ModuleNode(override val elements: List<StatementNode>): StatementListNode {
    override val childNodes = elements
    override val range = Range(0, elements.lastOrNull()?.range?.end ?: 0)
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): Completion {
        val itemList = elements
            .map { it.evaluateValueOrReturn { return it } }
            .foldRight(Completion.empty) { it, acc -> updateEmpty(acc, it) }
        if (itemList.type.isNormal && itemList.isEmpty) return Completion.normal(NullType)
        return itemList
    }
}
