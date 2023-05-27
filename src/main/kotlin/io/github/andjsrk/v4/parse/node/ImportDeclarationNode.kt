package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ImportDeclarationNode(
    val defaultBinding: DefaultImportBindingNode?,
    val nonDefaultBinding: NonDefaultImportBindingNode?,
    val moduleSpecifier: StringLiteralNode,
    startRange: Range,
    semicolonRange: Range?,
): DeclarationNode {
    override val childNodes get() = listOf(defaultBinding, nonDefaultBinding, moduleSpecifier)
    override val range = startRange..(semicolonRange ?: moduleSpecifier.range)
    override fun toString() =
        stringifyLikeDataClass(::defaultBinding, ::nonDefaultBinding, ::moduleSpecifier, ::range)
}
