package io.github.andjsrk.v4.parse

internal data class ParseContext(
    val allowModuleItem: Boolean = false,
    val allowIterationFlowControlSyntax: Boolean = false,
    val allowReturn: Boolean = false,
    val allowAwait: Boolean = true,
)
