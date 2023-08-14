package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.Realm
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.GeneratorType

class ExecutionContext(
    var realm: Realm,
    env: DeclarativeEnvironment? = null,
    var function: FunctionType? = null,
    var generator: GeneratorType<*>? = null,
) {
    lateinit var lexicalEnvironment: DeclarativeEnvironment
    init {
        if (env != null) lexicalEnvironment = env
    }
}
