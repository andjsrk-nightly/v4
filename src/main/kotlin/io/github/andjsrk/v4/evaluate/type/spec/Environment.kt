package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec

@EsSpec("Environment Record")
sealed class Environment(var outer: Environment? = null): Record {
    @EsSpec("HasBinding")
    abstract operator fun contains(name: String): Boolean
    @EsSpec("GetBindingValue")
    abstract fun getValue(name: String): Completion
}
