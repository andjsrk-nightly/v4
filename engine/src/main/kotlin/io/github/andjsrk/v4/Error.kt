package io.github.andjsrk.v4

import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.evaluate.error

/**
 * @param args An optional, non-empty argument list for the error.
 */
data class Error(val kind: ErrorKind, val range: Range, val args: List<String>? = null): Throwable() {
    init {
        if (args != null) require(args.isNotEmpty())
    }
    fun toErrorObject() =
        error(kind, *args?.toTypedArray() ?: emptyArray())
}
