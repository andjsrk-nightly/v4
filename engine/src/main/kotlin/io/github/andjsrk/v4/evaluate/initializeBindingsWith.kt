package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.UniqueFormalParametersNode

fun UniqueFormalParametersNode.initializeBindingsWith(args: List<LanguageType>, env: Environment?) =
    elements.initializeWith(
        args
            .iterator()
            .map { it.toNormal() }
            .toRestCollectedArrayIterator(elements),
        env,
    )
