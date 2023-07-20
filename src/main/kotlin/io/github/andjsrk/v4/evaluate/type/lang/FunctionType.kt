package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.Function
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.runningExecutionContext
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt

sealed class FunctionType(
    val name: PropertyKey?,
    requiredParameterCount: UInt,
    val env: DeclarativeEnvironment,
): ObjectType(
    lazy { Function.instancePrototype },
    mutableMapOf(
        "requiredParameterCount".sealedData(requiredParameterCount.toDouble().languageValue),
    ),
) {
    val realm = runningExecutionContext.realm
    abstract val isArrow: Boolean
    @EsSpec("[[Call]]")
    abstract fun _call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyNormalOrAbrupt
    // TODO: throw an error if thisArg is not provided but the function depends on it
}
