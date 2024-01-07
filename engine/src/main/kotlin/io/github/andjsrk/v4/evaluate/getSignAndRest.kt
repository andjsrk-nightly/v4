package io.github.andjsrk.v4.evaluate

/**
 * Returns a pair of (sign which will be determined by whether the string starts with '-') and the rest of the string.
 *
 * sign will be
 * - `-1` if the string starts with '-'
 * - `1` otherwise
 */
internal fun getSignAndRest(string: String) =
    if (string.startsWith('-')) -1 to string.substring(1)
    else 1 to string
