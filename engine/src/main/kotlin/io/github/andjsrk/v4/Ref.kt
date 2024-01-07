package io.github.andjsrk.v4

/**
 * A container that contains mutable data.
 * It is useful when sharing mutable data between functions.
 */
class Ref<T>(var value: T)
