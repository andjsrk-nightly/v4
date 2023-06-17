package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment

data class ExecutionContext(
    var lexicalEnvironment: DeclarativeEnvironment,
)
