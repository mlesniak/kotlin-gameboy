package com.mlesniak.gameboy

import java.lang.Integer.max
import java.lang.Integer.min

// Let's start as simple as possible.
class CPU(
    val code: UByteArray
) {
    // Should we use shorts or ints?
    var af: Int = 0x0000 // Accumulator and flag
    var bc: Int = 0x0000 // Can be used as two 8 bit registers.
    var de: Int = 0x0000 // Can be used as two 8 bit registers.
    var hl: Int = 0x0000 // Can be used as two 8 bit registers and as a 16 bit one. Index register.
    var sp: Int = 0x0000 // Stack pointer.
    var pc: Int = 0x0000 // Program counter.

    fun run() {
        while (true) {
            when (val opcode = nextOpcode()) {
                0x31 -> {
                    val low = nextOpcode()
                    val high = nextOpcode()
                    sp = high * 0x100 + low
                }

                else -> {
                    dump()
                    throw IllegalStateException("Unknown opcode ${opcode.hex()} at ${pc.hex()}")
                }
            }
        }
    }

    private fun dump() {
        println("PC ${pc.hex(4)}")
        println("SP ${sp.hex(4)}")
        val ds = (pc - 0x10).clamp(0)
        val es = (pc + 0x10).clamp(0, code.size)
        Debug.hexdump(code, ds..es)
    }

    private fun nextOpcode() = code[pc++].toInt()
}

fun Number.hex(padding: Int = 0) = "0x" + ("%X".format(this).padStart(padding, '0'))

fun Int.clamp(minVal: Int, maxVal: Int = Int.MAX_VALUE) = max(min(this, maxVal), minVal)
