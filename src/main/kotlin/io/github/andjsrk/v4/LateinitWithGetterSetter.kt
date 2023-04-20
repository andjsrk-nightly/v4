package io.github.andjsrk.v4

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LateinitWithGetterSetter<T: Any> internal constructor(
    private val get: FieldContainer<T>.() -> T = { field },
    private val set: FieldContainer<T>.(T) -> Unit = { field = it },
): ReadWriteProperty<Any, T> {
    private val container = FieldContainer<T>()
    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        container.get()
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        container.set(value)
    class FieldContainer<T: Any> {
        lateinit var field: T
    }
}
