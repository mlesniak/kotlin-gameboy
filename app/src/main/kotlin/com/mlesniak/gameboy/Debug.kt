package com.mlesniak.gameboy

import kotlin.math.min

object Debug {
    @Suppress("SimplifiableCallChain")
    fun hexdump(bytes: UByteArray, range: IntRange = 0..bytes.size) {
        val padding = "%x".format(bytes.size).length * 2
        val bytesPerRow = 16

        val visibleChars = 0x20..0x7f
        for (address in range step bytesPerRow) {
            val adr = "%0${padding}X".format(address)
            val row = bytes.slice(address until min(address + bytesPerRow, bytes.size))
            if (row.isEmpty()) {
                println("$adr")
                break
            }
            val hexBytes = row.map { "%02x".format(it.toByte()) }
                .chunked(bytesPerRow / 2)
                .map { chunk -> chunk.joinToString(" ") }
                .joinToString("  ")
            val chars = row
                .map { c -> if (c.toInt() in visibleChars) "%c".format(c.toByte()) else '.' }
                .joinToString("")

            println("$adr  $hexBytes  |$chars|")
        }
    }
}