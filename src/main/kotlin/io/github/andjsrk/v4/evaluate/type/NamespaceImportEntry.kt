package io.github.andjsrk.v4.evaluate.type

data class NamespaceImportEntry(
    override val sourceModule: String,
    override val localName: String,
): ImportEntry(sourceModule, localName)
