package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("EvaluateCall")
internal fun evaluateCall(value: LanguageType, ref: AbstractType, args: List<LanguageType>): NonEmptyNormalOrAbrupt {
    val thisValue =
        if (ref is Reference && ref.isProperty) ref.getThis()
        else NullType
    val func = value
        .requireToBe<FunctionType> { return it }
    // TODO: implement step 6
    return func._call(thisValue, args)
}
