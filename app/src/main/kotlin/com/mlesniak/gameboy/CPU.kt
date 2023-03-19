package com.mlesniak.gameboy

import com.mlesniak.gameboy.CPU.Flag.Carry
import com.mlesniak.gameboy.CPU.Flag.HalfCarry
import com.mlesniak.gameboy.CPU.Flag.Subtraction
import com.mlesniak.gameboy.CPU.Flag.Zero
import com.mlesniak.gameboy.debug.Debug
import com.mlesniak.gameboy.debug.decrementBytes
import com.mlesniak.gameboy.debug.hex
import com.mlesniak.gameboy.debug.num
import com.mlesniak.gameboy.debug.testBit
import com.mlesniak.gameboy.debug.toIgnoredSignInt
import java.nio.file.Files
import java.nio.file.Path
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.system.exitProcess

// Design decision: We don't use unsigned values since it involves a
// lot of annoying castings. Instead, we use normal (signed) values
// and take care to handle correct sign computation when necessary.
class CPU {
    private val MEMORY_SIZE = 0xFFFF
    private val ROM_PATH = Path.of("rom/boot.gb")

    // Note that not everything is real memory,
    // i.e. there is also a lot of memory-mapped
    // IO at specific address ranges, e.g. for
    // video and sound.
    //
    // 16 bit values are stored in little endian,
    // i.e. the lower byte is first.
    private val mem = ByteArray(MEMORY_SIZE)

    // Registers
    private var pc = 0x0000
    private var sp = 0x0000
    private var a: Byte = 0x00
    private var b: Byte = 0x00
    private var c: Byte = 0x00
    private var d: Byte = 0x00
    private var e: Byte = 0x00
    private var f: Byte = 0x00
    private var h: Byte = 0x00
    private var l: Byte = 0x00

    // Using the first four bits of register
    // f to store the values.
    enum class Flag(val pos: Int) {
        Zero(7),
        Subtraction(6),
        HalfCarry(5),
        Carry(4);
    }

    private fun set(vararg flags: Flag) {
        for (flag in flags) {
            f = f or (0x01 shl flag.pos).toByte()
        }
    }

    private fun unset(vararg flags: Flag) {
        for (flag in flags) {
            f = f and (0x01 shl flag.pos).toByte().inv()
        }
    }

    private fun isSet(flag: Flag): Boolean =
        f and (0x01 shl flag.pos).toByte() != 0x00.toByte()

    init {
        // Load ROM code into 0x00..0xFF. This is
        // static and independent of the actual
        // cartridge.
        val rom = Files.readAllBytes(ROM_PATH)
        rom.copyInto(mem, 0x0000)
    }

    // Execute the rom code at 0x0000. After the rom code
    // has been executed (logo has been shown, validated
    // and sound has been played), the actual cartridge
    // is executed (which we don't plan to implement).
    fun boot() {
        while (true) {
            executeNextInstruction()
        }
    }

    // The main simulation loop.
    private fun executeNextInstruction() {
        // println("\n" + "-".repeat(78))
        // dump()
        when (val opcode = nextByte().toIgnoredSignInt()) {
            // Prefix for extended commands
            0xCB -> {
                when (val opcode = nextByte().toIgnoredSignInt()) {
                    // RL C
                    0x11 -> {
                        val cn = (c.toIgnoredSignInt() shl 1).toByte()
                        if (cn == 0x00.toByte()) {
                            set(Zero)
                        } else {
                            unset(Zero)
                        }
                        if (c.testBit(7)) {
                            set(Carry)
                        } else {
                            unset(Carry)
                        }
                        unset(Subtraction, HalfCarry)
                        c = cn
                    }

                    // BIT 7,H
                    0x7C -> {
                        if (h.testBit(7)) {
                            set(Zero)
                        } else {
                            unset(Zero)
                        }
                        unset(Subtraction)
                        set(HalfCarry)
                    }

                    else -> abortWithUnknownOpcode(opcode)
                }
            }

            // PUSH BC
            0xC5 -> {
                mem[sp] = c.toUByte().toByte()
                mem[sp - 1] = b.toUByte().toByte()
                sp -= 2
            }

            // LD B,d8
            0x06 -> {
                b = nextByte()
            }

            // LD C,A
            0x4F -> {
                c = a
            }

            // CALL a16
            0xCD -> {
                val addr = nextByte().toIgnoredSignInt() + nextByte().toIgnoredSignInt() * 0x100
                mem[sp] = (pc / 0x100).toUByte().toByte()
                mem[sp - 1] = (pc % 0x100).toUByte().toByte()
                sp -= 2
                pc = addr
            }

            // LD A,(DE)
            0x1A -> {
                val addr = fromLittleEndian(e, d)
                a = mem[addr]
            }

            // LD DE,d16
            0x11 -> {
                e = nextByte()
                d = nextByte()
            }

            // LD ($FF00+a8),A or LDH (a8),A
            0xE0 -> {
                val addr = 0xFF00 + nextByte().toIgnoredSignInt()
                mem[addr] = a
            }

            // LD (HL),A
            0x77 -> {
                val addr = fromLittleEndian(l, h)
                mem[addr] = a
            }

            // INC C
            0x0C -> {
                c++
                if (c == 0x00.toByte()) {
                    set(Zero)
                } else {
                    unset(Zero)
                }
                unset(Subtraction)
                // NOTE Half-carry logic not implemented.
            }

            // LD ($FF00+C),A or LD (C),A
            0xE2 -> {
                val addr = 0xFF00 + c.toIgnoredSignInt()
                mem[addr] = a
            }

            // LD A,d8
            0x3E -> {
                a = nextByte()
            }

            // LD C,d8
            0x0E -> {
                c = nextByte()
            }

            // JR NZ,r8
            0x20 -> {
                val pcDelta = nextByte()
                if (isSet(Zero)) {
                    pc += pcDelta
                }
            }

            // LD (HL-),A
            0x32 -> {
                val addr = fromLittleEndian(l, h)
                mem[addr] = a

                val p = decrementBytes(h, l)
                h = p.first
                l = p.second
            }

            // LD HL,d16
            0x21 -> {
                l = nextByte()
                h = nextByte()
            }

            // XOR A
            // Z 0 0 0
            0xAF -> {
                a = 0
                set(Zero)
                unset(Carry, Subtraction, HalfCarry)
            }

            // LD SP,d16
            0x31 -> {
                val n1 = nextByte()
                val n2 = nextByte()
                sp = fromLittleEndian(n1, n2)
            }

            else -> abortWithUnknownOpcode(opcode)
        }
    }

    private fun abortWithUnknownOpcode(opcode: Int) {
        pc--
        println("Unknown opcode ${opcode.hex(2)} at position ${pc.hex(4)}")
        dump()
        // An exception just adds boilerplate output and is not helpful
        // since we control the call hierarchy completely, i.e. a stack
        // trace does not provide additional information anyway.
        exitProcess(1)
    }

    private fun fromLittleEndian(lowByte: Byte, highByte: Byte): Int {
        return highByte.toIgnoredSignInt() * 0x100 + lowByte.toIgnoredSignInt()
    }

    // Dump all registers and byte content around PC.
    private fun dump(addr: Int? = null) {
        val dumpStart = addr ?: pc
        println(
            """
            PC=${pc.hex(4)} SP=${sp.hex(4)}
            A=${a.hex(2)} B=${b.hex(2)} C=${c.hex(2)} D=${d.hex(2)} E=${e.hex(2)} H=${h.hex(2)} L=${l.hex(2)}
            Z${isSet(Zero).num()} N${isSet(Subtraction).num()} H${isSet(HalfCarry).num()} C${isSet(Carry).num()}
        """.trimIndent()
        )
        val start = (if (dumpStart < 0x10) 0 else dumpStart - 0x10).toInt()
        val end = (if (dumpStart + 0x10.toByte() > MEMORY_SIZE) MEMORY_SIZE else dumpStart + 0x10).toInt()
        Debug.hexdump(mem, start..end)
    }

    // Retrieve the next opcode from memory
    // and adjust PC.
    private fun nextByte(): Byte {
        val opcode = mem[pc]
        pc++
        return opcode
    }

    // The manufacturer logo is stored in 0xA8..0xD8 in 1bpp format.
    //
    // 00000104  ce ed 66 66 cc 0d 00 0b  03 73 00 83 00 0c 00 0d  |..ff.....s......|
    // 00000114  00 08 11 1f 88 89 00 0e  dc cc 6e e6 dd dd d9 99  |..........n.....|
    // 00000124  bb bb 67 63 6e 0e ec cc  dd dc 99 9f bb b9 33 3e  |..gcn.........3>|
    //
    // The first 24 bytes define rows 0..3, the second 24 bytes rows 4..7 based on the
    // following schema:
    //
    //     C 6 C 0 0 0 0 0 0 1 8 0
    //     E 6 C 0 3 0 0 0 0 1 8 0
    //     E 6 0 0 7 8 0 0 0 1 8 0
    //     D 6 D B 3 3 C D 8 F 9 E
    //
    //     D 6 D D B 6 6 E D 9 B 3
    //     C E D 9 B 7 E C D 9 B 3
    //     C E D 9 B 6 0 C D 9 B 3
    //     C 6 D 9 B 3 E C C F 9 E
    //
    // Finally, the binary representation of every nimble maps to pixels which are shown.
    fun showLogo() {
        val logo = mem.slice(0xA8..0xD7)
        for (y in 0..7) {
            val xs = y / 2 + (y / 4 * (logo.size / 2 - 2))
            val m = if (y > 3) logo.size else logo.size / 2
            for (x in xs until m step 2) {
                val nimble: Int =
                    if (y % 2 == 0) {
                        logo[x].toInt() / 0x10
                    } else {
                        logo[x].toInt() % 0x10
                    }
                val pixels = nimble
                    .toString(2)
                    .padStart(4, '0')
                    .replace('1', '█')
                    .replace('0', ' ')
                print("$pixels")
            }
            println()
        }
    }
}
