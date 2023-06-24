package io.github.andjsrk.v4.evaluate.type

open class CyclicModule(
    realm: Realm,
    val requestedModules: List<String>,
): Module(realm) {
    var status = ModuleStatus.NEW
    var evaluationError: Completion.Throw? = null
    val loadedModules = mutableMapOf<String, Module>()
}
