package io.github.andjsrk.v4

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.*

fun parseModule(sourceText: String, realm: Realm, sourceAbsolutePath: String): MaybeError<SourceTextModule, Error> {
    val parser = Parser(sourceText)
    val module = parser.parseModule()
    assert((module == null) == parser.hasError)
    if (module == null) return Invalid(parser.error!!)
    val requestedModules = module.moduleRequests()
    val importEntries = module.importEntries()
    val importBoundNames = importEntries.map { it.localName }
    val exportEntries = module.exportEntries()
    val localExportEntries = mutableListOf<ExportEntry>()
    val indirectExportEntries = mutableListOf<ExportEntry>()
    val starExportEntries = mutableListOf<ExportEntry>()
    for (entry in exportEntries) {
        when {
            entry.sourceModule == null -> {
                if (entry.localName !in importBoundNames) {
                    localExportEntries += entry
                    continue
                }
                when (
                    val importEntry = importEntries.find { it.localName == entry.localName }
                        ?: neverHappens()
                ) {
                    is NamespaceImportEntry -> localExportEntries += entry
                    is NormalImportEntry ->
                        indirectExportEntries += ExportEntry(
                            importEntry.sourceModule,
                            entry.exportName,
                            null,
                            importEntry.importName,
                        )
                }
            }
            entry.isAllReExport -> starExportEntries += entry
            else -> indirectExportEntries += entry
        }
    }

    return Valid(
        SourceTextModule(
            module,
            realm,
            requestedModules,
            importEntries,
            localExportEntries,
            indirectExportEntries,
            starExportEntries,
            sourceAbsolutePath,
        )
    )
}
