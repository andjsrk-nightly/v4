package io.github.andjsrk.v4.parse

data class ParseContext internal constructor(
    val allowModuleItem: Boolean = false,
    val allowIterationFlowControlStatement: Boolean = false,
    val allowReturn: Boolean = false,
    val allowAwait: Boolean = true,
)
