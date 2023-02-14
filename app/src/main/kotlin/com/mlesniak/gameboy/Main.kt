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
}

fun main() {
    val rom = Rom("rom/tetris.gb")
    rom.dump()
}
