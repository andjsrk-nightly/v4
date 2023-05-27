package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamedImportBindingNode(
    val elements: List<ImportOrExportSpecifierNode>,
    override val range: Range,
): NonDefaultImportBindingNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
}
