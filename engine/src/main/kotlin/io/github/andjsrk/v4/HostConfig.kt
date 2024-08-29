package io.github.andjsrk.v4

import io.github.andjsrk.v4.evaluate.type.*

/**
 * Note: Call [HostConfig.set] before evaluating a [Module].
 */
abstract class HostConfig {
    @EsSpec("HostLoadImportedModule")
    abstract fun loadImportedModule(module: CyclicModule, specifier: String, state: GraphLoadingState): EmptyOrAbrupt
    abstract fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt): Nothing
    open fun applyGlobalProperties(global: ObjectType) {}
    abstract fun display(value: LanguageType, raw: Boolean = false): String
    /**
     * Returns a [PromiseType] that fulfills after [ms] milliseconds.
     */
    abstract fun wait(ms: Int): PromiseType

    companion object {
        internal lateinit var value: HostConfig
        fun set(config: HostConfig) {
            value = config
        }
    }
}
