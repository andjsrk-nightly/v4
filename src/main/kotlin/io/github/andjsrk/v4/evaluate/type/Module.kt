package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

abstract class Module(
    val realm: Realm,
): Record {
    lateinit var environment: ModuleEnvironment
    lateinit var namespaceObject: ObjectType
    abstract fun resolveExport(exportName: String, resolveSet: MutableList<Pair<Module, String>> = mutableListOf()): ExportResolveResult?
}
