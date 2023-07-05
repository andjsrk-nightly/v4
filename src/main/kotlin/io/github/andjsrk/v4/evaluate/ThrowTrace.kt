package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

data class ThrowTrace(
    val value: LanguageType,
    val stackTrace: List<StackTraceItem> =
        executionContextStack.toList().asSequence()
            .filter { it.lexicalEnvironment is FunctionEnvironment }
            .map {
                val env = it.lexicalEnvironment as FunctionEnvironment
                StackTraceItem(env.function)
            }
            .toList()
) {
    companion object {
        var instance: ThrowTrace? = null
    }
}
