package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

@EsSpec("Reference Record")
data class Reference(
    /**
     * Note that `null` indicates `unresolvable`(spec).
     */
    val base: AbstractType?,
    /**
     * Note that value of the property is nullable because key must not be evaluated if [base] is [NullType] in optional chain.
     */
    val referencedName: PropertyKey?,
    val thisValue: LanguageType? = null,
    val isOptionalChain: Boolean = false,
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
    fun putValue(value: LanguageType): EmptyOrAbrupt {
        when {
            this.isUnresolvable -> return Completion.Throw(NullType/* ReferenceError */)
            this.isProperty -> {
                // primitives are immutable
                if (base !is ObjectType) return Completion.Throw(NullType/* TypeError */)
                returnIfAbrupt(base._set(referencedName!!, value, getThis())) { return it }
                return empty
            }
            else -> {
                require(base is DeclarativeEnvironment)
                require(referencedName is StringType)
                return base.setMutableBinding(referencedName.value, value)
            }
        }
    }
    @EsSpec("GetThisValue")
    fun getThis() =
        thisValue ?: base as LanguageType
}
