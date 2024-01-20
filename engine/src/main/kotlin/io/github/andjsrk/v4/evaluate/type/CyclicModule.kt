package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Cyclic Module Record")
abstract class CyclicModule(
    realm: Realm,
    val requestedModules: List<String>,
): Module(realm) {
    var status = Status.NEW
    var evaluationError: Completion.Throw? = null
    val loadedModules = mutableMapOf<String, Module>()
    var cycleRoot: CyclicModule? = null
    var topLevelCapability: PromiseType.Capability? = null
    var dfsIndex: Int? = null
    var dfsAncestorIndex: Int? = null
    var hasTopLevelAwait = false // will be updated after parsing
    var asyncEvaluation = false
    val asyncParentModules = mutableListOf<CyclicModule>()
    var pendingAsyncDependencies = 0
    @EsSpec("GetImportedModule")
    fun getImportedModule(specifier: String) =
        loadedModules[specifier]!!
    @EsSpec("InitializeEnvironment")
    abstract fun initializeEnvironment(): EmptyOrAbrupt
    override fun evaluate(): PromiseType {
        val module =
            if (status.isOneOf(Status.EVALUATING_ASYNC, Status.EVALUATED)) cycleRoot!!
            else this
        module.topLevelCapability?.let { return it.promise }
        val capability = PromiseType.Capability.new()
        module.topLevelCapability = capability
        val stack = Stack<CyclicModule>()
        val result = module.innerModuleEvaluation(stack, 0)
        if (result is Completion.Throw) {
            for (mod in stack) {
                mod.status = Status.EVALUATED
                evaluationError = result
            }
            capability.reject.call(null, listOf(result.value))
                .unwrap()
        } else {
            if (!asyncEvaluation) {
                capability.resolve.call(null, listOf(NullType))
                    .unwrap()
                assert(stack.isEmpty())
            }
        }
        runJobs()
        return capability.promise
    }
    @EsSpec("ExecuteAsyncModule")
    fun executeAsync() {
        val capability = PromiseType.Capability.new()
        val onFulfilled = functionWithoutThis { _ ->
            asyncModuleExecutionFulfilled()
        }
        TODO()
        execute(capability)
    }
    @EsSpec("GatherAvailableAncestors")
    fun gatherSynchronouslyExecutableAncestors(gathered: MutableSet<CyclicModule> = mutableSetOf()): MutableSet<CyclicModule> =
        gathered.apply {
            asyncParentModules.forEach {
                if (it !in gathered && it.cycleRoot?.evaluationError == null) {
                    it.pendingAsyncDependencies -= 1
                    if (it.pendingAsyncDependencies == 0) {
                        gathered += it
                        if (it.not { hasTopLevelAwait }) it.gatherSynchronouslyExecutableAncestors(gathered)
                    }
                }
            }
        }
    fun asyncModuleExecutionFulfilled() {
        if (status == Status.EVALUATED) return
        asyncEvaluation = false
        status = Status.EVALUATED
        topLevelCapability?.resolve?.call(null, listOf(NullType))
            ?.unwrap()
        val ancestors = gatherSynchronouslyExecutableAncestors()
            .sortedBy { TODO() }
        ancestors.forEach {
            if (it.hasTopLevelAwait) executeAsync()
            else {
                val result = execute()
                if (result is Completion.NonEmptyAbrupt) asyncModuleExecutionRejected(result.value)
            }
        }
    }
    fun asyncModuleExecutionRejected(reason: LanguageType) {
        if (status == Status.EVALUATED) return
        evaluationError = Completion.Throw(reason)
        status = Status.EVALUATED
        asyncParentModules.forEach { it.asyncModuleExecutionRejected(reason) }
        topLevelCapability?.reject?.call(null, listOf(reason))
            ?.unwrap()
    }
    override fun link(): EmptyOrAbrupt {
        val stack = Stack<CyclicModule>()
        val result = innerModuleLinking(stack, 0)
        if (result is Completion.Abrupt) {
            for (mod in stack) {
                mod.status = Status.UNLINKED
            }
            return result
        }
        assert(stack.isEmpty())
        return empty
    }
    fun loadRequestedModules(): PromiseType {
        val capability = PromiseType.Capability.new()
        val state = GraphLoadingState(capability)
        innerModuleLoading(state, this)
        return capability.promise
    }
    private fun innerModuleLoading(state: GraphLoadingState, module: Module) {
        if (module is CyclicModule && module.status == Status.NEW && module !in state.visited) {
            state.visited += module
            state.pendingModulesCount += module.requestedModules.size
            for (requested in module.requestedModules) {
                if (requested in module.loadedModules) {
                    innerModuleLoading(state, module.loadedModules.getValue(requested))
                } else {
                    HostConfig.value.loadImportedModule(this, requested, state)
                        .orReturn(HostConfig.value::onGotUncaughtAbrupt)
                }
            }
        }
    }
    fun finishLoadingImportedModule(specifier: String, payload: GraphLoadingState, result: MaybeAbrupt<Module>) {
        if (result is Completion.WideNormal) {
            if (specifier in loadedModules) assert(loadedModules[specifier] == result.value)
            else loadedModules[specifier] = result.value
        }
        continueModuleLoading(payload, result)
    }
    private fun continueModuleLoading(state: GraphLoadingState, completion: MaybeAbrupt<Module>) {
        if (state.not { isLoading }) return
        if (completion is Completion.WideNormal) innerModuleLoading(state, completion.value)
        else {
            state.isLoading = false
        }
    }
}
