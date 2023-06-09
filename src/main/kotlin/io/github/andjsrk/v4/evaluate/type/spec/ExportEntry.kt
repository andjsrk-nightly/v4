package io.github.andjsrk.v4.evaluate.type.spec

class ExportEntry(
    val sourceModule: String?,
    val exportName: String?,
    val localName: String?,
    val importName: String?,
) {
    val isAllReExport get() =
        exportName == null
}
