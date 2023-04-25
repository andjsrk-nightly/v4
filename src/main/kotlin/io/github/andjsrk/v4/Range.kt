package io.github.andjsrk.v4

data class Range(val start: Int, val end: Int) {
    operator fun rangeTo(other: Range) =
        Range(start, other.end)
    companion object {
        fun since(start: Int, length: Int) =
            Range(start, start + length)
    }
}
