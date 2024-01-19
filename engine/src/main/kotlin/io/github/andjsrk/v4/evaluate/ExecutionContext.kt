package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.GeneratorType

class ExecutionContext(
    var realm: Realm,
    env: DeclarativeEnvironment? = null,
    var function: FunctionType? = null,
    var generator: GeneratorType<*>? = null,
    val module: Module? = null,
) {
    lateinit var lexicalEnv: DeclarativeEnvironment
    init {
        if (env != null) lexicalEnv = env
    }
}
