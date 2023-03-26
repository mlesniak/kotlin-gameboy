package com.mlesniak.gameboy

import java.nio.file.Path

fun main() {
    val cpu = CPU(Path.of("rom/tetris.gb"))
    // TODO(mlesniak) We need to a load rom for the
    //                actual logo data in 0x104.
    // "Convert and load logo data from cart into Video RAM"
    cpu.boot()
}
