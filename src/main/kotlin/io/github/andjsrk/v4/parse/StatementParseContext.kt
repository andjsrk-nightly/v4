package io.github.andjsrk.v4.parse

data class StatementParseContext internal constructor(
    val allowModuleItem: Boolean = false,
    val allowIterationFlowControlStatement: Boolean = false,
)
