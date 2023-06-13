package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

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
    @EsSpec("InitializeReferencedBinding")
    fun initializeBinding(value: LanguageType) {
        assert(this.not { isUnresolvable })
        require(base is Environment)
        require(referencedName is StringType)
        base.initializeBinding(referencedName.value, value)
    }
    @EsSpec("PutValue")
    fun putValue(value: LanguageType): Completion {
        when {
            this.isUnresolvable -> return Completion.`throw`(NullType/* ReferenceError */)
            this.isProperty -> {
                TODO()
            }
            else -> {
                require(base is DeclarativeEnvironment)
                require(referencedName is StringType)
                return base.setMutableBinding(referencedName.value, value)
            }
        }
    }
}
