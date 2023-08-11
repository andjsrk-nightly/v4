package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ForInNode(
    val declaration: LexicalDeclarationWithoutInitializerNode,
    val target: ExpressionNode,
    override val body: StatementNode,
    startRange: Range,
): ForNode {
    override val childNodes get() = listOf(declaration, target, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::declaration, ::target, ::body, ::range)
    override fun evaluateLoop(): EvalFlow<LanguageType> {
        TODO()
    }
}
