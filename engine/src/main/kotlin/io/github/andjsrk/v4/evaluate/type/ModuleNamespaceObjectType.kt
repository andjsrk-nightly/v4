package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*

@EsSpec("Module Namespace Exotic Objects")
@EsSpec("ModuleNamespaceCreate")
class ModuleNamespaceObjectType(val module: Module, exports: List<String>): ObjectType(null) {
    val exports = exports.sorted()
    override fun _getOwnProperty(key: PropertyKey): MaybeThrow<Property?> {
        if (key is SymbolType) return super._getOwnProperty(key)
        require(key is StringType)
        if (key.nativeValue !in exports) return null.toWideNormal()
        val value = get(key)
            .orReturnThrow { return it }
        return DataProperty(value, configurable=false).toWideNormal()
    }
    override fun _hasProperty(key: PropertyKey): MaybeThrow<BooleanType> {
        if (key is SymbolType) return super._hasProperty(key)
        require(key is StringType)
        return (key.nativeValue in exports)
            .languageValue
            .toNormal()
    }
    override fun _get(key: PropertyKey, receiver: LanguageType): NonEmptyOrThrow {
        if (key is SymbolType) return super._get(key, receiver)
        require(key is StringType)
        if (key.nativeValue !in exports) return normalNull
        val binding = module.resolveExport(key.nativeValue)
        require(binding is ExportResolveResult.ResolvedBinding)
        val targetModule = binding.module
        val targetEnv = targetModule.env
            ?: return throwError(TODO())
        return targetEnv.getBindingValue(binding.bindingName)
    }
    override fun _set(key: PropertyKey, value: LanguageType, receiver: LanguageType): MaybeThrow<BooleanType?> {
        return BooleanType.FALSE.toNormal()
    }
    override fun _delete(key: PropertyKey): EmptyOrThrow {
        if (key is SymbolType) return super._delete(key)
        return empty
    }
}
