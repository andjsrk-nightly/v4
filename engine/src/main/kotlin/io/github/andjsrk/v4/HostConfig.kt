package io.github.andjsrk.v4

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

/**
 * Note: Call [HostConfig.set] before evaluating a [Module].
 */
abstract class HostConfig {
    @EsSpec("HostLoadImportedModule")
    abstract fun loadImportedModule(module: CyclicModule, specifier: String, state: GraphLoadingState): EmptyOrAbrupt
    abstract fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt): Nothing
    open fun applyGlobalProperties(global: ObjectType) {}
    abstract fun display(value: LanguageType, raw: Boolean = false): String

    companion object {
        lateinit var value: HostConfig
        fun set(config: HostConfig) {
            value = config
        }
    }
}
