package io.github.andjsrk.v4

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// https://stackoverflow.com/a/47948047/16815911
class MutableLazy<T>(val initializer: () -> T): ReadWriteProperty<Any?, T> {
    private object UninitializedValue
    private var _value: Any? = UninitializedValue
    @Suppress("UNCHECKED_CAST")
    val value get() =
        _value as T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (_value != UninitializedValue) return value
        synchronized(this) {
            if (_value == UninitializedValue) _value = initializer()
            return value
        }
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(this) {
            this._value = value
        }
    }

    companion object {
        fun <T> from(lazy: Lazy<T>)  =
            MutableLazy { lazy.value }
    }
}
