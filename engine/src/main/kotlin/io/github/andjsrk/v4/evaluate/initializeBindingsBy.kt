package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.NonEmptyOrAbrupt
import io.github.andjsrk.v4.parse.node.UniqueFormalParametersNode

fun UniqueFormalParametersNode.initializeBindingsBy(args: Iterator<NonEmptyOrAbrupt>, env: Environment?) =
    elements.initializeBy(
        args.toRestCollectedArrayIterator(elements)
            .map {
                lazyFlow { it }
            },
        env,
    )
