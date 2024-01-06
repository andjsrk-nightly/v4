package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PromiseType
import io.github.andjsrk.v4.parse.node.ModuleNode

@EsSpec("Source Text Module Record")
class SourceTextModule(
    /**
     * See [`[[ECMAScriptCode]]`](https://tc39.es/ecma262/multipage/ecmascript-language-scripts-and-modules.html#table-additional-fields-of-source-text-module-records).
     */
    @EsSpec("-")
    val node: ModuleNode,
    realm: Realm,
    requestedModules: List<String>,
    val importEntries: List<ImportEntry>,
    val localExportEntries: List<ExportEntry>,
    val indirectExportEntries: List<ExportEntry>,
    val starExportEntries: List<ExportEntry>,
    absolutePath: String,
): CyclicModule(realm, requestedModules, absolutePath) {
    var context: ExecutionContext? = null
    var importMeta: ObjectType? = null
    override fun getExportedNames(exportStarSet: MutableSet<SourceTextModule>): List<String> {
        if (this in exportStarSet) return emptyList()
        exportStarSet += this
        val exportedNames = mutableListOf<String>()
        for (entry in localExportEntries) exportedNames += entry.exportName!!
        for (entry in indirectExportEntries) exportedNames += entry.exportName!!
        for (entry in starExportEntries) {
            val requestedModule = getImportedModule(entry.sourceModule!!)
            val starNames = requestedModule.getExportedNames(exportStarSet)
            for (name in starNames) exportedNames += name
        }
        return exportedNames
    }
    override fun resolveExport(exportName: String, resolveSet: MutableList<Pair<Module, String>>): ExportResolveResult? {
        assert(status != Status.NEW)
        val resolve = this to exportName
        if (resolveSet.any { it == resolve }) return null // circular import
        resolveSet += resolve
        for (entry in localExportEntries) {
            if (exportName == entry.exportName) return ExportResolveResult.ResolvedBinding(this, entry.localName!!)
        }
        for (entry in indirectExportEntries) {
            if (exportName == entry.exportName) {
                val importedModule = getImportedModule(entry.sourceModule!!)
                return importedModule.resolveExport(entry.importName!!, resolveSet)
            }
        }
        var starResolution: ExportResolveResult.ResolvedBinding? = null
        for (entry in starExportEntries) {
            val importedModule = getImportedModule(entry.sourceModule!!)
            when (val resolution = importedModule.resolveExport(exportName, resolveSet)) {
                ExportResolveResult.Ambiguous -> return resolution
                is ExportResolveResult.ResolvedBinding -> when {
                    starResolution == null -> starResolution = resolution
                    resolution.module != starResolution.module -> return ExportResolveResult.Ambiguous
                    resolution.bindingName != starResolution.bindingName -> return ExportResolveResult.Ambiguous
                }
                null -> {}
            }
        }
        return starResolution
    }
    override fun initializeEnvironment(): EmptyOrAbrupt {
        for (entry in indirectExportEntries) {
            val resolution = resolveExport(entry.exportName!!)
            if (resolution == null || resolution !is ExportResolveResult.ResolvedBinding) return throwError(SyntaxErrorKind.AMBIGUOUS_EXPORT, TODO())
            TODO()
        }
        // TODO: implement step 1
        environment = ModuleEnvironment(realm.globalEnv)
        for (ie in importEntries) {
            val importedModule = getImportedModule(ie.sourceModule)
            if (ie is NamespaceImportEntry) {
                val namespace = ie.localName
            }
        }
        val moduleContext = ExecutionContext(realm, environment)
        context = moduleContext
        executionContextStack.addTop(moduleContext)
        instantiateBlockDeclaration(node, environment)
        executionContextStack.removeTop()
        return empty
    }
    override fun execute(capability: PromiseType.Capability?): EmptyOrAbrupt {
        if (hasTopLevelAwait) {
            requireNotNull(capability)
            capability.asyncBlockStart(node, ExecutionContext(realm, environment))
        } else {
            executionContextStack.addTop(ExecutionContext(realm, environment))
            val res = node.evaluate()
            executionContextStack.removeTop()
            if (res is Completion.Abrupt) return res
        }
        return empty
    }
}
