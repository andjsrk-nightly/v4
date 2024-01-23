package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.GeneratorType

data class ExecutionContext(
    var realm: Realm,
    var lexicalEnv: DeclarativeEnvironment? = null,
    var function: FunctionType? = null,
    var generator: GeneratorType<*>? = null,
    val module: Module? = null,
    var codeEvaluationState: SimpleLazyFlow<MaybeEmptyOrAbrupt>? = null,
) {
    var lexicalEnvNotNull: DeclarativeEnvironment
        get() = lexicalEnv!!
        set(value) {
            lexicalEnv = value
        }
}
