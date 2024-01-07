package io.github.andjsrk.v4.error

enum class ReferenceErrorKind(override val message: String): ErrorKind {
    NOT_DEFINED("% is not defined"),
    SUPER_ALREADY_CALLED("Super constructor may only be called once"),
    ACCESSED_UNINITIALIZED_VARIABLE("Cannot access '%' before initialization"),
}
