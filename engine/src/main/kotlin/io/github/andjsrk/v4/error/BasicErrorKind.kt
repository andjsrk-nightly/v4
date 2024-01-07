package io.github.andjsrk.v4.error

enum class BasicErrorKind(override val message: String): ErrorKind {
    CANNOT_FIND_MODULE("Cannot find module '%'"),
}
