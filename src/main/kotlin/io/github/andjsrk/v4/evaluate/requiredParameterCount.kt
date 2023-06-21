package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.parse.node.*

val UniqueFormalParametersNode.requiredParameterCount get(): UInt {
    return elements.fold(0u) { acc, it ->
        when (it) {
            is RestNode -> return acc
            is NonRestNode -> acc + (if (it.default != null) 0u else 1u)
        }
    }
}
