package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.parse.exportEntries
import io.github.andjsrk.v4.parse.importEntries
import io.github.andjsrk.v4.parse.node.ModuleNode

data class SourceText(
    val moduleNode: ModuleNode,
    var importMeta: ObjectType? = null,
): Record {
    val importEntries by lazy { moduleNode.importEntries() }
    val exportEntries by lazy { moduleNode.exportEntries() }
}
