package com.mlesniak.gameboy

import java.nio.file.Path

fun main() {
    val rom = Rom(Path.of("rom/boot.gb"), Path.of("rom/tetris.gb"))
    // rom.dumpTitle()
    // rom.showLogo()
    // rom.dumpBootRom()
    val cpu = CPU()
    cpu.execute(rom.bootRom())
}
