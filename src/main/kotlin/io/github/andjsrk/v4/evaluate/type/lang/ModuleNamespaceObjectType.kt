package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.*

class ModuleNamespaceObjectType(val module: Module, val exports: List<String>): ObjectType(null) {
    override fun _getOwnProperty(key: PropertyKey): Property? {
        if (key is SymbolType) return super._getOwnProperty(key)
        if (key.string() !in exports) return null
        val value = get(key)
            .orReturn { return it }
    }
    override fun _get(key: PropertyKey, receiver: LanguageType): NonEmptyOrAbrupt {
        if (key is SymbolType) return super._get(key, receiver)
        val stringKey = key.string()
        if (stringKey !in exports) return normalNull
        val binding = module.resolveExport(stringKey)
        require(binding is ExportResolveResult.ResolvedBinding)
        val targetModule = binding.module
        val targetEnv = targetModule.environment ?: return throwError(TODO())
        return targetEnv.getBindingValue(binding.bindingName)
    }
    override fun _set(key: PropertyKey, value: LanguageType, receiver: LanguageType): MaybeAbrupt<BooleanType?> {
        return BooleanType.FALSE.toNormal()
    }
    override fun _delete(key: PropertyKey): EmptyOrAbrupt {
        if (key is SymbolType) return super._delete(key)
        return empty
    }
}
