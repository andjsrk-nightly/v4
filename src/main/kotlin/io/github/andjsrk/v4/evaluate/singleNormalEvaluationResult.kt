package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.toNormal

internal fun singleNormalEvaluationResult(value: LanguageType) =
    EvalFlow {
        `return`(value.toNormal())
    }
