package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class AllReExportDeclarationNode(
    override val moduleSpecifier: StringLiteralNode,
    range: Range,
    semicolonRange: Range?,
): ExportDeclarationWithModuleSpecifierNode {
    override val childNodes get() = listOf(moduleSpecifier)
    override val range = range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::moduleSpecifier, ::range)
}
