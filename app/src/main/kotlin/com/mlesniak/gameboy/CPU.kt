package com.mlesniak.gameboy

import com.mlesniak.gameboy.debug.Debug
import com.mlesniak.gameboy.debug.clamp
import com.mlesniak.gameboy.debug.hex
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

class CPU {
    private val MEMORY_SIZE = 0xFFFF.toUInt()
    private val ROM_PATH = Path.of("rom/boot.gb")

    // Note that not everything is real memory,
    // i.e. there is also a lot of memory-mapped
    // IO at specific address ranges, e.g. for
    // video and sound.
    private val mem = UByteArray(MEMORY_SIZE.toInt())

    // Registers.
    private var pc = 0x0000.toUInt()

    init {
        // Load ROM code into 0x00..0xFF. This is
        // static and independent of the actual
        // cartridge.
        val rom = Files.readAllBytes(ROM_PATH).toUByteArray()
        rom.copyInto(mem, 0x0000)
    }

    // Execute the rom code at 0x0000. After the rom code
    // has been executed (logo has been shown, validated
    // and sound has been played), the actual cartridge
    // is executed (which we don't plan to implement).
    fun boot() {
        execute()
    }

    // The main simulation loop.
    private fun execute() {
        when (val opcode = nextOpcode()) {
            else -> {
                pc--
                println("Unknown opcode ${opcode.hex(2)} at position ${pc.hex(4)}")
                dump()
                // An exception just adds boilerplate output and is not helpful
                // since we control the call hierarchy completely, i.e. a stack
                // trace does not provide additional information anyway.
                exitProcess(1)
            }
        }
    }

    // Dump all registers and byte content around PC.
    private fun dump() {
        println(
            """
            PC ${pc.hex(4)}
        """.trimIndent()
        )
        val start = (if (pc < 0x10.toUByte()) 0.toUInt() else pc - 0x10.toUByte()).toInt()
        val end = (if (pc + 0x10.toUByte() > MEMORY_SIZE) MEMORY_SIZE else pc + 0x10.toUInt()).toInt()
        Debug.hexdump(mem, start..end)
    }

    // Retrieve the next opcode from memory
    // and adjust PC.
    private fun nextOpcode(): UByte {
        val opcode = mem[pc.toInt()]
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
