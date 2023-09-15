package io.github.andjsrk.v4.evaluate.type

sealed interface ExportResolveResult {
    data class ResolvedBinding(
        val module: Module,
        val bindingName: String,
    ): ExportResolveResult, Record
    object Ambiguous: ExportResolveResult
}
