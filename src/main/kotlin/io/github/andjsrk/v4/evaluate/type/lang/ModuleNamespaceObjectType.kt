package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("Module Namespace Exotic Objects")
@EsSpec("ModuleNamespaceCreate")
class ModuleNamespaceObjectType(val module: Module, exports: List<String>): ObjectType(null) {
    val exports = exports.sorted()
    override fun _getOwnProperty(key: PropertyKey): MaybeAbrupt<Property?> {
        if (key is SymbolType) return super._getOwnProperty(key)
        require(key is StringType)
        if (key.value !in exports) return null.toWideNormal()
        val value = get(key)
            .orReturn { return it }
        return DataProperty(value, configurable=false).toWideNormal()
    }
    override fun _hasProperty(key: PropertyKey): MaybeAbrupt<BooleanType> {
        if (key is SymbolType) return super._hasProperty(key)
        require(key is StringType)
        return BooleanType.from(key.value in exports).toNormal()
    }
    override fun _get(key: PropertyKey, receiver: LanguageType): NonEmptyOrAbrupt {
        if (key is SymbolType) return super._get(key, receiver)
        require(key is StringType)
        if (key.value !in exports) return normalNull
        val binding = module.resolveExport(key.value)
        require(binding is ExportResolveResult.ResolvedBinding)
        val targetModule = binding.module
        val targetEnv = targetModule.environment
            ?: return throwError(TODO())
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
