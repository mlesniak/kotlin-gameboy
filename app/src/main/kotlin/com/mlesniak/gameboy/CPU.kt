package com.mlesniak.gameboy

import java.lang.Integer.max
import java.lang.Integer.min

// Let's start as simple as possible.
class CPU {
    // Should we use shorts? or bytes? or ints?
    var af: Int = 0x0000 // Accumulator and flag
    var bc: Int = 0x0000 // Can be used as two 8 bit registers.
    var de: Int = 0x0000 // Can be used as two 8 bit registers.
    var hl: Int = 0x0000 // Can be used as two 8 bit registers and as a 16 bit one. Index register.
    var sp: Int = 0x0000 // Stack pointer.
    var pc: Int = 0x0000 // Program counter.

    fun execute(code: UByteArray) {
        while (true) {
            val opcode = code[pc].toInt()
            when (opcode) {
                else -> {
                    val ds = (pc - 0x10).clamp(0)
                    val es = (pc + 0x10).clamp(0, code.size)
                    Debug.hexdump(code, ds..es)
                    throw IllegalStateException("Unknown opcode 0x${opcode.hex()} at 0x${pc.hex()}")
                }
            }
            pc++
        }
    }
}

fun Number.hex() = "%X".format(this)

fun Int.clamp(minVal: Int, maxVal: Int = Int.MAX_VALUE) = max(min(this, maxVal), minVal)
