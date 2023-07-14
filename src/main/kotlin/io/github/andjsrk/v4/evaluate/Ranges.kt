package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.NumberType

internal object Ranges {
    val unsignedInteger = 0L..NumberType.MAX_SAFE_INTEGER.toLong()
    val relativeIndex = NumberType.MIN_SAFE_INTEGER.toLong()..NumberType.MAX_SAFE_INTEGER.toLong()
    val index = unsignedInteger
    val radix = 2L..36L
    val int8 = int(8)
    val int16 = int(16)
    val int32 = int(32)
    val uint8 = uint(8)
    val uint16 = uint(16)
    val uint32 = uint(32)

    private fun int(bits: Int): LongRange {
        val size = 1L.shl(bits - 1)
        return -size until size
    }
    private fun uint(bits: Int): LongRange {
        require(bits in 8..32)
        val size = 1L.shl(bits)
        return 0..size
    }
}
