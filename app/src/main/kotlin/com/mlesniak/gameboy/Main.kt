package com.mlesniak.gameboy

import java.nio.file.Files
import java.nio.file.Path

class Rom(
    private val filename: String,
) {
    private var bytes: ByteArray

    val size: Int
        get() {
            return bytes.size
        }

    init {
        bytes = Files.readAllBytes(Path.of(filename))
    }

    fun debug() {
        hexdump(bytes)
    }
}

@Suppress("SimplifiableCallChain")
fun hexdump(bytes: ByteArray) {
    val padding = "%x".format(bytes.size).length * 2
    val bytesPerRow = 16

    for (address in bytes.indices step bytesPerRow) {
        val adr = "%0${padding}X".format(address)
        val row = bytes.slice(address until address + bytesPerRow)
        val hexBytes = row.map { "%02x".format(it) }
            .chunked(bytesPerRow / 2)
            .map { chunk ->
                chunk.joinToString(" ")
            }
            .joinToString("  ")
        val chars = row.map { c ->
            if (c in 0x20..0x7f) "%c".format(c) else '.'
        }.joinToString("")

        println("$adr  $hexBytes  |$chars|")
    }
}

fun main() {
    val rom = Rom("rom/tetris.gb")
    rom.debug()
}
