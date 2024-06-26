package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*

data class ExecutionContext(
    var realm: Realm,
    var lexicalEnv: DeclarativeEnvironment? = null,
    var function: FunctionType? = null,
    var generator: GeneratorType<*>? = null,
    val module: Module? = null,
    var codeEvaluationState: SimpleLazyFlow<Completion.FromFunctionBody<LanguageType>>? = null,
    var privateEnv: PrivateEnvironment? = null,
) {
    /**
     * @see lexicalEnv
     */
    var lexicalEnvNotNull: DeclarativeEnvironment
        get() = lexicalEnv!!
        set(value) {
            lexicalEnv = value
        }
}
