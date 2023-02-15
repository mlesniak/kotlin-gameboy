package com.mlesniak.gameboy

import com.mlesniak.gameboy.Debug.hexdump
import java.nio.file.Files
import java.nio.file.Path

class Rom(
    filename: String,
) {
    private var bytes: ByteArray

    init {
        bytes = Files.readAllBytes(Path.of(filename))
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
}

fun main() {
    val rom = Rom("rom/tetris.gb")
    rom.dumpTitle()
    rom.dumpLogo()
}
