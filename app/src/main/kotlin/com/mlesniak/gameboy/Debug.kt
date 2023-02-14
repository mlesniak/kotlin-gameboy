package com.mlesniak.gameboy

object Debug {
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
}