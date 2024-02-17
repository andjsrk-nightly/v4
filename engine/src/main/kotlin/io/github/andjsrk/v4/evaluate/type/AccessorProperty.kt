package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*

@EsSpec("accessor property")
data class AccessorProperty(
    var get: FunctionType?,
    var set: FunctionType?,
    override var enumerable: Boolean,
    override var configurable: Boolean,
): Property() {
    constructor(
        get: FunctionType? = null,
        set: FunctionType? = null,
        enumerable: Boolean? = null,
        configurable: Boolean? = null,
    ): this(
        get,
        set,
        enumerable ?: ENUMERABLE_DEFAULT,
        configurable ?: CONFIGURABLE_DEFAULT,
    )
    override fun clone() = copy()
    override fun getValue(thisValue: LanguageType, key: PropertyKey) =
        get?.call(thisValue)
            ?: throwError(TypeErrorKind.NO_GETTER, key.string())
    override fun setValue(thisValue: LanguageType, key: PropertyKey, value: LanguageType): EmptyOrThrow {
        set?.call(thisValue, listOf(value))
            ?.orReturnThrow { return it }
            ?: return throwError(TypeErrorKind.NO_SETTER, key.string())
        return empty
    }
    override fun toDescriptorObject(): ObjectType {
        val obj = ObjectType.createNormal()
        obj.createDataProperty("get".languageValue, get ?: NullType)
        obj.createDataProperty("set".languageValue, set ?: NullType)
        return super.toDescriptorObject(obj)
    }
}

inline fun getter(name: String, crossinline block: (thisArg: LanguageType) -> NonEmptyOrThrow) =
    BuiltinFunctionType(name, 0u) fn@ { thisArg, _ ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        block(thisArg)
    }
inline fun setter(name: String, crossinline block: (thisArg: LanguageType, value: LanguageType) -> EmptyOrThrow) =
    BuiltinFunctionType(name, 1u) fn@ { thisArg, args ->
        if (thisArg == null) return@fn throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        block(thisArg, args[0])
            .orReturnThrow { return@fn it }
        normalNull
    }
