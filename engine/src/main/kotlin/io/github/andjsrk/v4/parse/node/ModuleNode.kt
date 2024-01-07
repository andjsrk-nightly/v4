package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateStatements
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.NonEmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.normalizeToNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ModuleNode(override val elements: List<StatementNode>): StatementListNode {
    override val childNodes = elements
    override val range = Range(0, elements.lastOrNull()?.range?.end ?: 0)
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): NonEmptyOrAbrupt {
        val res = evaluateStatements(this)
            .orReturn { return it }
        return res.normalizeToNormal()
    }
}
