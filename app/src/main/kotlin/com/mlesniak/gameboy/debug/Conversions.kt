package com.mlesniak.gameboy.debug

import kotlin.experimental.and
import kotlin.math.max
import kotlin.math.min

fun Int.hex(padding: Int = 0) = ("%X".format(this).padStart(padding, '0'))

fun Byte.hex(padding: Int = 0) = ("%X".format(this).padStart(padding, '0'))

fun Byte.binary(padding: Int = 0) = ("%b".format(this).padStart(padding, '0'))

fun Int.clamp(minVal: Int, maxVal: Int = Int.MAX_VALUE) = max(min(this, maxVal), minVal)

fun Boolean.num(): String = if (this) "1" else "0"

fun Byte.testBit(pos: Int): Boolean =
    this and (1 shl pos).toByte() != 0.toByte()

fun decrementBytes(high: Byte, low: Byte): Pair<Byte, Byte> {
    var nh = high
    var nl = low.toUByte().toInt() - 1
    if (nl < 0) {
        nl = 0x00
        nh--
    }
    return nh to nl.toByte()
}

// Do we need a better name since it overrides the normal toInt method?
fun Byte.toIgnoredSignInt(): Int = this.toUByte().toInt()