package io.github.andjsrk.v4.evaluate.type

sealed interface ExportResolveResult: Record {
    data class ResolvedBinding(
        val module: Module,
        val bindingName: String,
    ): ExportResolveResult
    object Ambiguous: ExportResolveResult
}
