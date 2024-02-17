package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.not

@EsSpec("Reference Record")
data class Reference(
    val base: AbstractType?,
    /**
     * Note that value of the property is nullable because key must not be evaluated if [base] is [NullType] in optional chain.
     */
    val referencedName: PropertyKey?,
    val thisValue: LanguageType? = null,
    val isOptionalChain: Boolean = false,
): Record, EvaluationResult {
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
    fun initializeBinding(value: LanguageType): EmptyOrThrow {
        assert(this.not { isUnresolvable })
        require(base is Environment)
        require(referencedName is StringType)
        return base.initializeBinding(referencedName.value, value)
    }
    @EsSpec("PutValue")
    fun putValue(value: LanguageType): EmptyOrThrow {
        return when {
            isUnresolvable -> {
                require(referencedName is StringType)
                throwError(ReferenceErrorKind.NOT_DEFINED, referencedName.value)
            }
            isProperty -> {
                if (base !is ObjectType) return throwError(TypeErrorKind.PRIMITIVE_IMMUTABLE)
                if (referencedName is PrivateName) return base.privateSet(referencedName, value)
                base._set(referencedName!!, value, getThis())
                    .orReturnThrow { return it }
                empty
            }
            else -> {
                require(base is DeclarativeEnvironment)
                require(referencedName is StringType)
                base.setMutableBinding(referencedName.value, value)
            }
        }
    }
    @EsSpec("GetThisValue")
    fun getThis() =
        thisValue ?: base as LanguageType

    companion object {
        @EsSpec("MakePrivateReference")
        fun private(baseValue: LanguageType, name: String): Reference {
            val env = runningExecutionContext.privateEnv
            requireNotNull(env)
            val privateName = resolvePrivateIdentifier(name, env)
            return Reference(baseValue, privateName)
        }
    }
}
