package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

@EsSpec("ExportEntry Record")
data class ExportEntry(
    /**
     * See [`[[ModuleRequest]]`](https://tc39.es/ecma262/multipage/ecmascript-language-scripts-and-modules.html#exportentry-record).
     */
    @EsSpec("-")
    val sourceModule: String?,
    val exportName: String?,
    val localName: String?,
    val importName: String?,
) {
    val isAllReExport get() =
        exportName == null
}
