package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.Realm
import io.github.andjsrk.v4.evaluate.type.lang.AbstractFunctionType

data class ExecutionContext(
    var lexicalEnvironment: DeclarativeEnvironment,
    var realm: Realm,
    var function: AbstractFunctionType? = null,
)
