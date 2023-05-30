package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class AllExportDeclarationNode(
    val moduleSpecifier: StringLiteralNode,
    startRange: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val childNodes get() = listOf(moduleSpecifier)
    override val range = startRange..(semicolonRange ?: moduleSpecifier.range)
    override fun toString() =
        stringifyLikeDataClass(::moduleSpecifier, ::range)
}
