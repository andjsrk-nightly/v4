package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.Environment

@EsSpec("GetThisEnvironment")
tailrec fun findThisEnvironment(env: Environment = runningExecutionContext.lexicalEnv): DeclarativeEnvironment? {
    if (env is DeclarativeEnvironment && env.hasThisBinding()) return env
    val outer = env.outer ?: return null
    return findThisEnvironment(outer)
}
