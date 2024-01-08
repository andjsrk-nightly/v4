package io.github.andjsrk.v4.cli

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

open class DefaultHostConfig: HostConfig() {
    override fun loadImportedModule(module: CyclicModule, specifier: String, state: GraphLoadingState): EmptyOrAbrupt {
        val path = Path(moduleAbsolutePaths[module]!!).join(specifier)
        val (actualPath, sourceText) = path.resolveAndRead()
            .orReturn { return it }
            .value

        val loadedModule = when (val moduleOrErr = parseModule(sourceText, module.realm)) {
            is Valid -> moduleOrErr.value
            is Invalid -> {
                println(moduleOrErr.value)
                exitProcess(1)
            }
        }
        moduleAbsolutePaths[loadedModule] = actualPath.absolutePathString()
        loadedModule.initializeEnvironment()
            .orReturn { return it }
        module.finishLoadingImportedModule(specifier, state, loadedModule.toWideNormal())
        return empty
    }
    override fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt): Nothing {
        exitWithAbrupt(abrupt)
    }
    override fun applyGlobalProperties(global: ObjectType) {
        for ((key, desc) in globalObj.properties) {
            global.definePropertyOrThrow(key, desc)
        }
    }
}
