package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

data class Binding(
    val isMutable: Boolean,
    var value: LanguageType?,
    // if `isIndirect` is true, `module` and `exportedLocalName` must not be a `null`
    val isIndirect: Boolean = false,
    val module: Module? = null,
    /**
     * [ExportEntry.localName] of the binding.
     *
     * @see SourceTextModule.resolveExport
     */
    val exportedLocalName: String? = null,
) {
    val isInitialized get() =
        value != null
}
