package io.github.andjsrk.v4

class Range(val start: Int, val end: Int) {
    companion object {
        fun since(start: Int, length: Int) =
            Range(start, start + length)
    }
}
