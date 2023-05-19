package io.github.andjsrk.v4.tokenize

class Source(private val source: String) {
    var pos = 0
        private set
    fun advance(): Char {
        val curr = curr
        if (pos < source.length) pos++
        return curr
    }
    val curr get() =
        source.getOrNull(pos) ?: endOfInput
    fun peek(relativePos: Int = 1) =
        source.getOrNull(pos + relativePos) ?: endOfInput
    inner class CheckPoint {
        private val pos = this@Source.pos
        fun load() {
            this@Source.pos = pos
        }
    }
    companion object {
        const val endOfInput = (-1).toChar()
    }
}
