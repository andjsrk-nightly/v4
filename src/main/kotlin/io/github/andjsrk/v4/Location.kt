package io.github.andjsrk.v4

class Location(val start: Int, val end: Int) {
    companion object {
        fun since(start: Int, length: Int) =
            Location(start, start + length)
    }
}
