package io.github.andjsrk.v4.evaluate.type

/**
 * Represents an unofficial(not specified in ES specification) value so the value can be treated as a value of a normal completion.
 */
data class GeneralSpecValue<V>(val value: V): AbstractType {
    fun withValue(new: V) =
        GeneralSpecValue(new)
}
