package com.mlesniak.gameboy

import org.graalvm.collections.EconomicMap
import java.lang.Integer.max
import java.lang.Integer.min

// Let's start as simple as possible.
class CPU(
    private val code: UByteArray
) {
    // Should we use bytes or ints?
    var a: Int = 0x00 // Accumulator
    var f: Int = 0x00 // Flags: zero subtraction half-carry carry 0000
    var hl: Int = 0x00
    var sp: Int = 0x0000 // Stack pointer.
    var pc: Int = 0x0000 // Program counter.

    // Memory including VRAM (0x8000..0x9FFF).
    val mem: IntArray = IntArray(0x9FFF + 0x01)

    // TODO(mlesniak) Refactor once we have a better feeling for the
    //                structure.
    fun run() {
        while (true) {
            when (val opcode = nextOpcode()) {
                // LD SP, nnnn
                0x31 -> {
                    val low = nextOpcode()
                    val high = nextOpcode()
                    sp = high * 0x100 + low
                }
                // XOR A
                0xAF -> {
                    a = 0x00
                    f = f or 0b10000000
                }
                // LD HL, nnnn
                0x21 -> {
                    // Little endian.
                    hl = nextOpcode() + nextOpcode() * 0x100
                }
                // LD (HL-),A
                0x32 -> {
                    mem[hl] = a
                    hl--
                }

                else -> {
                    dump()
                    throw IllegalStateException("Unknown opcode ${opcode.hex(2)} at ${pc.hex(4)}")
                }
            }
        }
    }

    private fun dump() {
        println("PC ${pc.hex(4)}")
        println("SP ${sp.hex(4)}")
        println("A  ${a.hex(2)}")
        println("F  ${f.binary(8).substring(0..5)}")
        println("HL ${hl.hex(4)}")
        val ds = (pc - 0x10).clamp(0)
        val es = (pc + 0x10).clamp(0, code.size)
        Debug.hexdump(code, ds..es)
    }

    private fun nextOpcode() = code[pc++].toInt()
}

fun Number.hex(padding: Int = 0) = "0x" + ("%X".format(this).padStart(padding, '0'))
fun Int.binary(padding: Int = 0) = "0b" + (Integer.toBinaryString(this).padStart(padding, '0'))

fun Int.clamp(minVal: Int, maxVal: Int = Int.MAX_VALUE) = max(min(this, maxVal), minVal)
