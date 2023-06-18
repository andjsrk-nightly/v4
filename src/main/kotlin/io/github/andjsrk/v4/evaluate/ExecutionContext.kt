package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.Realm

data class ExecutionContext(
    var lexicalEnvironment: DeclarativeEnvironment,
    var realm: Realm,
)
