package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.PromiseType
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

abstract class CyclicModule(
    realm: Realm,
    val requestedModules: List<String>,
    var absolutePath: String,
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
    val absolutePathWithoutExtension get() =
        absolutePath.removeSuffix(languageExtension)
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
        return capability.promise
    }
    fun executeAsync() {
        val capability = PromiseType.Capability.new()
        TODO()
        execute(capability)
    }
    @EsSpec("Link")
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
                    loadImportedModule(requested, state)
                        .orReturn {
                            eprintln(it)
                            println(it.value?.display())
                            exitProcess(1)
                        }
                }
            }
        }
    }
    @EsSpec("HostLoadImportedModule")
    private fun loadImportedModule(specifier: String, state: GraphLoadingState): EmptyOrAbrupt {
        val path = Path(absolutePath).join(specifier)
        val (actualPath, sourceText) = path.resolveAndRead()
            .orReturn { return it }
            .value
        val module = when (val moduleOrErr = parseModule(sourceText, realm, actualPath.absolutePathString())) {
            is Valid -> moduleOrErr.value
            is Invalid -> {
                eprintln(moduleOrErr.value)
                exitProcess(1)
            }
        }
        module.initializeEnvironment()
            .orReturn { return it }
        finishLoadingImportedModule(specifier, state, module.toWideNormal())
        return empty
    }
    private fun finishLoadingImportedModule(specifier: String, payload: GraphLoadingState, result: MaybeAbrupt<Module>) {
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
