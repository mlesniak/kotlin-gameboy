package com.mlesniak.gameboy

import java.nio.file.Path

fun main() {
    val cpu = CPU(Path.of("rom/tetris.gb"))
    cpu.boot()
}
