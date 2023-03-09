package com.mlesniak.gameboy.debug

import kotlin.math.max
import kotlin.math.min

fun UInt.hex(padding: Int = 0) = "0x" + ("%X".format(this.toInt()).padStart(padding, '0'))

fun UByte.hex(padding: Int = 0) = "0x" + ("%X".format(this.toByte()).padStart(padding, '0'))

fun UInt.clamp(minVal: UInt, maxVal: UInt = UInt.MAX_VALUE) = max(min(this, maxVal), minVal)
