package io.github.andjsrk.v4.parse

internal data class ParseContext(
    val allowModuleItem: Boolean = false,
    val allowIterationFlowControlStatement: Boolean = false,
    val allowReturn: Boolean = false,
    val allowAwait: Boolean = true,
)
