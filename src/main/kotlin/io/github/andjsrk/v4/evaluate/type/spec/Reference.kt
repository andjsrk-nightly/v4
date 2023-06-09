package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Reference Record")
data class Reference(
    /**
     * Note that `null` indicates `unresolvable`(spec).
     */
    val base: AbstractType?,
    val referencedName: LanguageType/* StringType or SymbolType */,
    val thisValue: LanguageType? = null,
): Record {
    @EsSpec("IsPropertyReference")
    val isProperty get() =
        base != null && base !is Environment
    @EsSpec("IsUnresolvableReference")
    inline val isUnresolvable get() =
        base == null
    @EsSpec("IsSuperReference")
    inline val isSuper get() =
        thisValue != null
}
