package io.github.andjsrk.v4

import io.github.andjsrk.v4.parse.Parser

fun Parser.createErrorMsg() =
    """
        Error occurred: $error
    """.trimIndent() + stackTrace?.toErrorMessagePart()

internal fun List<StackTraceElement>.toErrorMessagePart() =
    "\nStack trace:\n${joinToString("\n") { "    $it" }}"
