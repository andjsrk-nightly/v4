package io.github.andjsrk.v4.evaluate.type

data class NormalImportEntry(
    override val sourceModule: String,
    val importName: String,
    override val localName: String,
): ImportEntry(sourceModule, localName)
