package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.and
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamedReExportDeclarationNode(
    override val moduleSpecifier: StringLiteralNode,
    val specifiers: List<ImportOrExportSpecifierNode>,
    range: Range,
    semicolonRange: Range?,
): ExportDeclarationWithModuleSpecifierNode {
    override val childNodes get() = moduleSpecifier and specifiers
    override val range = range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::moduleSpecifier, ::specifiers, ::range)
    override fun evaluate() = TODO()
}
