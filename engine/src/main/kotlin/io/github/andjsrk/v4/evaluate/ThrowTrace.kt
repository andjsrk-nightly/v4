package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.LanguageType

data class ThrowTrace(
    val value: LanguageType,
    val stackTrace: List<StackTraceItem> =
        executionContextStack.toList().asSequence()
            .filter { it.lexicalEnvNotNull is FunctionEnvironment }
            .map {
                val env = it.lexicalEnvNotNull as FunctionEnvironment
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
