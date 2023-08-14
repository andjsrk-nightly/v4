package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
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
): CyclicModule(realm, requestedModules) {
    var context: ExecutionContext? = null
    var importMeta: ObjectType? = null
    @EsSpec("InitializeEnvironment")
    fun initializeEnvironment() {
        // TODO: implement step 1
        environment = ModuleEnvironment(realm.globalEnv)
        // TODO: implement step 7
        val moduleContext = ExecutionContext(realm, environment)
        context = moduleContext
        executionContextStack.addTop(moduleContext)
        instantiateBlockDeclaration(node, environment)
        executionContextStack.removeTop()
    }
    @EsSpec("ExecuteModule")
    fun executeModule(): EmptyOrAbrupt {
        executionContextStack.addTop(ExecutionContext(realm, environment))
        val res = node.evaluate()
        executionContextStack.removeTop()
        return if (res is Completion.Abrupt) res else empty
    }
}
