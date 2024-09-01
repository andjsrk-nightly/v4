package io.github.andjsrk.v4.evaluate

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// https://stackoverflow.com/a/47948047/16815911
internal class MutableLazy<T>(initializer: () -> T): ReadWriteProperty<Any?, T>, Lazy<T> {
    private var initializer: (() -> T)? = initializer
    @Volatile
    private var _value: T? = null
    override var value
        get() =
            synchronized(this) {
                val currValue = _value
                val init = initializer
                @Suppress("UNCHECKED_CAST")
                if (init == null) currValue as T
                else {
                    val value = init()
                    _value = value
                    initializer = null
                    value
                }
            }
        set(value) =
            synchronized(this) {
                _value = value
                initializer = null
            }
    override fun isInitialized() =
        initializer == null
    override fun getValue(thisRef: Any?, property: KProperty<*>) =
        value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    companion object {
        fun <T> from(lazy: Lazy<T>) =
            MutableLazy { lazy.value }
    }
}
