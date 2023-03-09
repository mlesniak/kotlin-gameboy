package com.mlesniak.gameboy

import java.lang.Integer.max
import java.lang.Integer.min

// Let's start as simple as possible.
class CPU(
    private val code: UByteArray
) {
    // Should we use bytes or ints?
    var a: Int = 0x00 // Accumulator
    var b: Int = 0x00 // Accumulator
    var c: Int = 0x00
    var d: Int = 0x00
    var e: Int = 0x00
    var f: Int = 0x00 // Flags: zero subtraction half-carry carry 0000

    //                            7      6           5         4
    var hl: Int = 0x00
    var sp: Int = 0x0000 // Stack pointer.
    var pc: Int = 0x0000 // Program counter.

    // Memory including VRAM (0x8000..0x9FFF).
    val mem: IntArray = IntArray(0xFFFF)

    fun run() {
        // Copy code into memory.
        // TODO(mlesniak) Execute code from memory and not code
        for (i in code.indices) {
            mem[i] = code[i].toInt()
        }
        try {
            _run()
        } catch (e: Exception) {
            dump()
            throw (e)
        }
    }

    // TODO(mlesniak) Refactor once we have a better feeling for the
    //                structure.
    fun _run() {
        while (true) {
            // println()
            // dump()
            // println("next opcode ${code[pc].toInt().hex(2)}")
            // print("?")
            // readLine()
            when (val opcode = nextOpcode()) {
                // LD A,H
                0x7C -> {
                    // TODO(mlesniak) Add this next :)
                }
                // INC H
                0x24 -> {
                    hl = (hl / 0x100 + 1)  * 0x100 + hl % 0x100
                }
                // DEC E
                0x1D -> {
                    e--
                    f = if (e == 0) {
                        f or 0b10000000
                    } else {
                        f and (0x01 shl 7).inv()
                    }
                }
                // LDH A,(a8)
                0xF0 -> {
                    val addr = 0xFF00 + nextOpcode()
                    a = mem[addr]
                }
                // LD E,d8
                0x1E -> {
                    e = nextOpcode()
                }
                // INC B
                0x04 -> {
                    // Ignore registers for now.
                    b++
                }
                // LD D,A
                0x57 -> {
                    d = a
                }
                // LD H,A
                0x67 -> {
                    hl = hl % 0x100 + a * 0x100
                }
                // JR r8
                0x18 -> {
                    var dst = nextOpcode()
                    if (dst > 127) {
                        // Signed numbers are stored in two-complements.
                        dst = -(((dst - 1).toByte() ).toInt()).inv()
                    }
                    pc += dst
                }
                // LD L,d8
                0x2E -> {
                    // TODO(mlesniak) Or is it little endian stored and we should update H?
                    val v = nextOpcode()
                    hl = (hl / 0x100) * 0x100 + v
                }
                // DEC C
                0x0D -> {
                    c--
                    if (c < 0) {
                        c = 0
                    }
                    f = if (c == 0) {
                        f or 0b10000000
                    } else {
                        f and (0x01 shl 7).inv()
                    }
                }
                // JR Z,r8
                0x28 -> {
                    // signed jump destination
                    var dst = nextOpcode()
                    if (dst > 127) {
                        dst = -(((dst - 1).toByte() ).toInt()).inv()
                    }
                    val targetPC = pc + dst
                    if (f and (0x01 shl 7) != 0) {
                        pc = targetPC
                    }
                }
                // DEC A
                0x3D -> {
                    a--
                    if (a < 0) {
                        a = 0
                    }
                    f = if (a == 0) {
                        f or 0b10000000
                    } else {
                        f and (0x01 shl 7).inv()
                    }
                }
                // LD (a16),A
                0xEA -> {
                    val addr = nextOpcode() + nextOpcode() * 0x100
                    mem[addr] = a
                }
                // CP d8
                0xFE -> {
                    // CP A with n
                    val tmp = a - nextOpcode()
                    f = if (tmp == 0) {
                        f or 0b10000000
                    } else {
                        f and (0x01 shl 7).inv()
                    }
                }
                // LD A,E
                0x7B -> {
                    a = e
                }
                // INC DE
                0x13 -> {
                    val et = e + 0x01
                    e = et % 0x100
                    d += if (et >= 0x100) 0x01 else 0x00
                }
                // RET
                0xC9 -> {
                    // dump()
                    pc = mem[sp+1]*0x100 + mem[sp + 2]
                    sp += 2
                    // println("=== RET")
                    // dump()
                    // readLine()
                }
                // LD (HL+),A
                0x22 -> {
                    mem[hl] = a
                    hl++
                }
                // INC HL
                0x23 -> {
                    hl++
                }
                // DEC B  Zero, substract, half-carry (todo)
                0x05 -> {
                    b -= 1
                    f = if (b == 0) {
                        f or 0b10000000
                    } else {
                        f and (0x01 shl 7).inv()
                    }
                    f = f or 0b01000000
                }
                // POP BC
                0xC1 -> {
                    c = mem[sp]
                    b = mem[sp + 1]
                    sp += 2
                }
                // RLA , sets carry and zero bit
                0x17 -> {
                    // Rotate n left through Carry flag.
                    // Affect Z and C
                    val bit = (a and (0x01 shl 7)) != 0x00
                    c = (a shl 1) % 0x100
                    // Zero
                    val zeroBitCond = a == 0x00
                    f = if (zeroBitCond) {
                        f or 0b10000000
                    } else {
                        f and (0x01 shl 7).inv()
                    }
                }
                // PUSH BC
                0xC5 -> {
                    mem[sp] = b
                    mem[sp - 1] = c
                    sp -= 2
                }
                // LD B,d8
                0x06 -> {
                    b = nextOpcode()
                }
                // LD C,A
                0x4F -> {
                    c = a
                }
                // CALL a16
                0xCD -> {
                    val newpc = nextOpcode() + 0x100 * nextOpcode()
                    // println("NEWPC ${newpc.hex(4)}")
                    // pc = mem[sp] + mem[sp + 1] * 0x100
                    //
                    // 100 sp+1
                    //  99 sp
                    //
                    mem[sp] = pc % 0x100
                    mem[sp - 1] = pc / 0x100
                    // println("[sp]=${mem[sp].hex(2)}  [sp-1]=${mem[sp-1].hex(2)}")
                    sp -= 2
                    pc = newpc
                }
                // LD A,(DE)
                0x1A -> {
                    val addr = d * 0x100 + e
                    a = mem[addr]
                }
                // LD DE, nnnn
                0x11 -> {
                    e = nextOpcode()
                    d = nextOpcode()
                }
                // LHD (nn), A
                0xE0 -> {
                    val adr = 0xFF00 + nextOpcode()
                    mem[adr] = a
                }
                // LD (HL),A
                0x77 -> {
                    mem[hl] = a
                }
                // INC C
                0x0C -> {
                    c++
                }
                // LD (C), A
                0xE2 -> {
                    val adr = 0xFF00 + c
                    mem[adr] = a
                }
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
                // LD c, nn
                0x0E -> {
                    c = nextOpcode()
                }
                // LD A, nn
                0x3E -> {
                    a = nextOpcode()
                }
                // JR NZ, n (signed)
                0x20 -> {
                    val delta = nextOpcode().toByte()
                    if (f and (0x01 shl 7) != 0) {
                        pc += delta
                    }
                }
                // Extended command.
                0xCB -> {
                    when (val opcode = nextOpcode()) {
                        // RL C
                        0x11 -> {
                            // Rotate n left through Carry flag.
                            // Affect Z and C
                            val bit = (c and (0x01 shl 7)) != 0x00
                            c = (c shl 1) % 0x100
                            // Zero
                            val zeroBitCond = c == 0x00
                            f = if (zeroBitCond) {
                                f or 0b10000000
                            } else {
                                f and (0x01 shl 7).inv()
                            }

                            // set carry to flag.
                            if (bit) {
                                f = f or (0x01 shl 4)
                            } else {
                                f = f and (0x01 shl 4).inv()
                            }
                        }
                        // BIT 7,H Zero and Half-Carry Flag
                        // Test if bit 7 is set in H
                        0x7C -> {
                            val h = hl shr 8
                            val zeroBitCond = h and (0x01 shl 7) != 0x00
                            if (zeroBitCond) {
                                f = f or 0b10000000
                            } else {
                                f = f and (0x01 shl 7).inv()
                            }
                            // HC
                            f = f and (0x01 shl 5).inv()
                        }

                        else -> {
                            pc -= 2
                            dump()
                            throw IllegalStateException("Unknown opcode 0xCB ${opcode.hex(2)} at ${pc.hex(4)}")
                        }
                    }
                }

                else -> {
                    pc -= 1
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
        println("B  ${b.hex(2)}")
        println("C  ${c.hex(2)}")
        println("D  ${d.hex(2)}")
        println("E  ${e.hex(2)}")
        println("F  ${f.binary(8).substring(0..5)}")
        println("HL ${hl.hex(4)}")
        val ds = (pc - 0x10).clamp(0)
        val es = (pc + 0x10).clamp(0, code.size)
        Debug.hexdump(code, ds..es)
    }

    private fun nextOpcode() = code[pc++].toInt()
}

fun Number.hex(padding: Int = 0) = "0x" + ("%X".format(this).padStart(padding, '0'))
fun Int.binary(padding: Int = 8) = "0b" + (Integer.toBinaryString(this).padStart(padding, '0'))

fun Int.clamp(minVal: Int, maxVal: Int = Int.MAX_VALUE) = max(min(this, maxVal), minVal)
