package io.github.andjsrk.v4

import io.github.andjsrk.v4.error.ErrorKind

/**
 * @param args An optional, non-empty argument list for the error.
 */
data class Error(val kind: ErrorKind, val range: Range, val args: List<String>? = null) {
    init {
        if (args != null) require(args.isNotEmpty())
    }
}
