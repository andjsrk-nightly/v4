package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.lang.*
import kotlin.math.min

abstract class Module(
    val realm: Realm,
): Record {
    var environment: ModuleEnvironment? = null
    @JvmField
    var namespaceObject: ObjectType? = null
    @EsSpec("GetExportedNames")
    abstract fun getExportedNames(exportStarSet: MutableSet<SourceTextModule> = mutableSetOf()): List<String>
    @EsSpec("ResolveExport")
    abstract fun resolveExport(exportName: String, resolveSet: MutableList<Pair<Module, String>> = mutableListOf()): ExportResolveResult?
    @EsSpec("Evaluate")
    abstract fun evaluate(): PromiseType
    @EsSpec("ExecuteModule")
    abstract fun execute(capability: PromiseType.Capability? = null): EmptyOrAbrupt
    abstract fun link(): EmptyOrAbrupt

    @EsSpec("GetModuleNamespace")
    fun getNamespaceObject(): ObjectType {
        if (namespaceObject == null) {
            val names = getExportedNames()
            val unambiguousNames = names.filter { resolveExport(it) is ExportResolveResult.ResolvedBinding }
            namespaceObject = ModuleNamespaceObjectType(this, unambiguousNames)
        }
        return namespaceObject!!
    }
    @EsSpec("InnerModuleEvaluation")
    fun innerModuleEvaluation(stack: Stack<CyclicModule>, index: Int): MaybeAbrupt<GeneralSpecValue<Int>> {
        var index = index // intentionally shadows the parameter to change its value
        if (this !is CyclicModule) {
            val promise = evaluate()
            assert(promise.state != null)
            if (promise.state == PromiseType.State.REJECTED) return Completion.Throw(promise.result!!)
            return index.toGeneralWideNormal()
        }
        if (status.isOneOf(Status.EVALUATING_ASYNC, Status.EVALUATED)) {
            return evaluationError ?: index.toGeneralWideNormal()
        }
        if (status == Status.EVALUATING) return index.toGeneralWideNormal()
        status = Status.EVALUATING
        dfsIndex = index
        dfsAncestorIndex = index
        pendingAsyncDependencies = 0
        index += 1
        stack.addTop(this)
        for (requested in requestedModules) {
            val requestedModule = getImportedModule(requested)
            index = requestedModule.innerModuleEvaluation(stack, index)
                .orReturn { return it }
                .value
            if (requestedModule is CyclicModule) {
                var requestedModule: CyclicModule = requestedModule
                if (requestedModule.status == Status.EVALUATING) {
                    dfsAncestorIndex = min(dfsAncestorIndex!!, requestedModule.dfsAncestorIndex!!)
                } else {
                    requestedModule = requestedModule.cycleRoot!!
                    requestedModule.evaluationError?.let { return it }
                }
                if (requestedModule.asyncEvaluation) {
                    pendingAsyncDependencies += 1
                    requestedModule.asyncParentModules += this
                }
            }
        }
        if (pendingAsyncDependencies > 0 || hasTopLevelAwait) {
            asyncEvaluation = true
            if (pendingAsyncDependencies == 0) executeAsync()
        } else {
            execute()
                .orReturn { return it }
        }
        assert(dfsAncestorIndex!! <= dfsIndex!!)
        if (dfsAncestorIndex == dfsIndex) {
            var done = false
            while (!done) {
                val requestedModule = stack.removeTop()
                requestedModule.status =
                    if (requestedModule.not { asyncEvaluation }) Status.EVALUATED
                    else Status.EVALUATING_ASYNC
                if (requestedModule == this) done = true
                requestedModule.cycleRoot = this
            }
        }
        return index.toGeneralWideNormal()
    }
    @EsSpec("InnerModuleLinking")
    fun innerModuleLinking(stack: Stack<CyclicModule>, index: Int): MaybeAbrupt<GeneralSpecValue<Int>> {
        var index = index
        if (this !is CyclicModule) {
            link().orReturn { return it }
            return index.toGeneralWideNormal()
        }
        if (status.isOneOf(Status.LINKING, Status.LINKED, Status.EVALUATING_ASYNC, Status.EVALUATED)) {
            return index.toGeneralWideNormal()
        }
        status = Status.LINKING
        dfsIndex = index
        dfsAncestorIndex = index
        index += 1
        stack.addTop(this)
        for (requested in requestedModules) {
            val requestedModule = getImportedModule(requested)
            index = innerModuleLinking(stack, index)
                .orReturn { return it }
                .value
            if (requestedModule is CyclicModule && requestedModule.status == Status.LINKING) {
                dfsAncestorIndex = min(dfsAncestorIndex!!, requestedModule.dfsAncestorIndex!!)
            }
        }
        initializeEnvironment()
            .orReturn { return it }
        if (dfsAncestorIndex == dfsIndex) {
            var done = false
            while (!done) {
                val requestedModule = stack.removeTop()
                requestedModule.status = Status.LINKED
                if (requestedModule == this) done = true
            }
        }
        return index.toGeneralWideNormal()
    }

    enum class Status {
        NEW,
        UNLINKED,
        LINKING,
        LINKED,
        EVALUATING,
        EVALUATING_ASYNC,
        EVALUATED
    }
}
