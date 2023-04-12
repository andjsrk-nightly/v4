package io.github.andjsrk.v4.tokenize

class Source(private val source: String) {
    var pos = 0
        private set
    fun dispose() {
        pos = source.length
    }
    fun advance(): Char {
        val curr = curr
        if (pos < source.length) pos++
        return curr
    }
    fun advanceWhile(check: (Char) -> Boolean): Char {
        while (check(curr)) advance()
        return curr
    }
    val curr get() =
        source.getOrNull(pos) ?: endOfInput
    fun peek(relativePos: Int = 1) =
        source.getOrNull(pos + relativePos) ?: endOfInput
    companion object {
        const val endOfInput = (-1).toChar()
    }
}
