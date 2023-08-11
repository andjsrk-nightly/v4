package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamespaceImportDeclarationNode(
    override val moduleSpecifier: StringLiteralNode,
    val binding: IdentifierNode,
    startRange: Range,
    semicolonRange: Range?,
): ImportDeclarationNode {
    override val childNodes get() = listOf(moduleSpecifier, binding)
    override val range = startRange..binding.range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::moduleSpecifier, ::binding, ::range)
    override fun evaluate() = TODO()
}
