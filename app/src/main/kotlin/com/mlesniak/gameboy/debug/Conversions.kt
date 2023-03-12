package com.mlesniak.gameboy.debug

import kotlin.math.max
import kotlin.math.min

fun Int.hex(padding: Int = 0) = "0x" + ("%X".format(this).padStart(padding, '0'))

fun Byte.hex(padding: Int = 0) = "0x" + ("%X".format(this).padStart(padding, '0'))

fun Int.clamp(minVal: Int, maxVal: Int = Int.MAX_VALUE) = max(min(this, maxVal), minVal)
