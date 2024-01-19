package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

data class ThrowTrace(
    val value: LanguageType,
    val stackTrace: List<StackTraceItem> =
        executionContextStack.toList().asSequence()
            .filter { it.lexicalEnv is FunctionEnvironment }
            .map {
                val env = it.lexicalEnv as FunctionEnvironment
                StackTraceItem(env.function)
            }
            .toList(),
    val nativeStackTrace: List<StackTraceElement> =
        Error().stackTrace.asList()
) {
    companion object {
        var instance: ThrowTrace? = null
    }
}
