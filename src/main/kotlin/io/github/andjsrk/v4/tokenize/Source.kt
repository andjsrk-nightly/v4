package io.github.andjsrk.v4.tokenize

class Source(private val source: String) {
    var pos = 0
        private set
    fun dispose() {
        pos = source.length
    }
    fun advance(): Char {
        val curr = curr
        pos++
        return curr
    }
    val curr get() =
        source[pos]
    fun peek(relativePos: Int = 1) =
        source[pos + relativePos]
}
