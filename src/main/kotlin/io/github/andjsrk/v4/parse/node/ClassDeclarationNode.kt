package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ClassDeclarationNode(
    override val name: IdentifierNode,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode, DeclarationNode {
    override fun toString() =
        stringifyLikeDataClass(::name, ::elements, ::range)
}
