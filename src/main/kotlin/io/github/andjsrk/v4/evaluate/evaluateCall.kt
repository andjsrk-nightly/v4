package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("EvaluateCall")
internal fun evaluateCall(func: LanguageType, ref: AbstractType, args: List<LanguageType>): NonEmptyNormalOrAbrupt {
    val thisValue =
        if (ref is Reference && ref.isProperty) ref.getThis()
        else NullType
    if (func !is FunctionType) return Completion.Throw(NullType/* TypeError */)
    // TODO: implement step 6
    return func._call(thisValue, args)
}
