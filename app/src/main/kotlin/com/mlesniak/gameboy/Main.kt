package com.mlesniak.gameboy

fun main() {
    val cpu = CPU()
    // TODO(mlesniak) We need to a load rom for the
    //                actual logo data in 0x104.
    cpu.boot()
}
