package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.not
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// https://stackoverflow.com/a/47948047/16815911
internal class MutableLazy<T>(val initializer: () -> T): ReadWriteProperty<Any?, T> {
    private object UninitializedValue
    private var _value: Any? = UninitializedValue
    var value
        @Suppress("UNCHECKED_CAST")
        get() = _value as T
        set(value) {
            _value = value
        }
    private inline val isInitialized get() =
        _value != UninitializedValue
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (this.isInitialized) return value
        synchronized(this) {
            if (this.not { isInitialized }) value = initializer()
            return value
        }
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(this) {
            this.value = value
        }
    }

    companion object {
        fun <T> from(lazy: Lazy<T>) =
            MutableLazy { lazy.value }
    }
}
