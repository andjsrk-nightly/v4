package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.MAX_CODE_POINT
import io.github.andjsrk.v4.evaluate.type.NumberType

internal class NamedRange(val name: String, val range: LongRange) {
    companion object {
        val unsignedInteger = NamedRange("an unsigned integer", 0L..NumberType.MAX_SAFE_INTEGER.toLong())
        val relativeIndex = NamedRange("a relative index", NumberType.MIN_SAFE_INTEGER.toLong()..NumberType.MAX_SAFE_INTEGER.toLong())
        val radix = NamedRange("a radix", 2L..36L)
        val codePoint = NamedRange("a code point", 0L..MAX_CODE_POINT)

        val int8 = int(8)
        val int16 = int(16)
        val int32 = int(32)
        val uint8 = uint(8)
        val uint16 = uint(16)
        val uint32 = uint(32)

        private fun int(bits: Int): NamedRange {
            val size = 1L.shl(bits - 1)
            return NamedRange("int$bits", -size until size)
        }
        private fun uint(bits: Int): NamedRange {
            require(bits in 8..32)
            val size = 1L.shl(bits)
            return NamedRange("uint$bits", 0..size)
        }
    }
}
