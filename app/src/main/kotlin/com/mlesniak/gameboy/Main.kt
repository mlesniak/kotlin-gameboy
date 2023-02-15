package com.mlesniak.gameboy

import com.mlesniak.gameboy.Debug.hexdump
import java.lang.Byte.parseByte
import java.nio.file.Files
import java.nio.file.Path

class Rom(
    filename: String,
) {
    private var bytes: UByteArray

    init {
        bytes = Files.readAllBytes(Path.of(filename)).toUByteArray()
    }

    fun dump() {
        hexdump(bytes)
    }

    // 0104-0133 - Nintendo Logo
    fun dumpLogo() {
        hexdump(bytes, 0x104..0x0133)
    }

    fun dumpTitle() {
        hexdump(bytes, 0x134..0x143)
    }

    fun bytes() = bytes
}

fun main() {
    val rom = Rom("rom/tetris.gb")
    rom.dumpLogo()

    // Hack mode on...
    // val colors = parseByte(0xce.toByte(), 0xed.toByte())
    // println(colors)
    val tile1 = rom.bytes().slice(0x104..0x133).chunked(16)[0]
    // val tile1 = listOf(0x7C, 0x7C, 0x00, 0xC6, 0xC6,0x00, 0x00, 0xFE, 0xC6,0xC6,0x00,0xC6,0xC6,0x00,0x00,0x00).map {it.toUByte()}
    println(tile1.map { "%02X".format(it.toByte()) }.joinToString(" "))
    val lines = tile1.chunked(2).map { line ->
        val l = parseByte(line[0], line[1])
        l
    }
    lines.forEach {
        val k = it.joinToString("").replace('0', '.')
        println(k)
    }

}

// For each line, the first byte defines the least significant bits of the color
// numbers for each pixel, and the second byte defines the upper bits of the color
// numbers. In either case, Bit 7 is the leftmost pixel, and Bit 0 the rightmost.
//
// 00000104  ce ed 66 66 cc 0d 00 0b  03 73 00 83 00 0c 00 0d  |..ff.....s......|
// 00000114  00 08 11 1f 88 89 00 0e  dc cc 6e e6 dd dd d9 99  |..........n.....|
// 00000124  bb bb 67 63 6e 0e ec cc  dd dc 99 9f bb b9 33 3e  |..gcn.........3>|
fun parseByte(b1: UByte, b2: UByte): List<Int> {
    println("Working on ${"%02X".format(b1.toByte())} ${"%02X".format(b2.toByte())}")
    // TODO(mlesniak) Use actual bit operations later on.
    val s1 = b1.toInt().toString(2).replace("-", "").padStart(8, '0')
    val s2 = b2.toInt().toString(2).replace("-", "").padStart(8, '0')
    // higher bit is in second byte.
    println(s1)
    println(s2)

    val pairs = s1.zip(s2)
    println(pairs)
    // (Kotlin destructuring at work)
    val colors = pairs.map { (v1, v2) ->
        (v2.code - 48) * 2 + (v1.code - 48)
    }
    println("colors $colors")
    return colors
}