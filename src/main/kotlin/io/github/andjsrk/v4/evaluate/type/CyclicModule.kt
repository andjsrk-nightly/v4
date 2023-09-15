package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

open class CyclicModule(
    realm: Realm,
    val requestedModules: List<String>,
): Module(realm) {
    var status = ModuleStatus.NEW
    var evaluationError: Completion.Throw? = null
    val loadedModules = mutableMapOf<String, Module>()
    override fun resolveExport(exportName: String, resolveSet: MutableList<Pair<Module, String>>): ExportResolveResult? {
        TODO()
    }
    @EsSpec("GetImportedModule")
    fun getImportedModule(specifier: String) =
        loadedModules[specifier]!!
}
