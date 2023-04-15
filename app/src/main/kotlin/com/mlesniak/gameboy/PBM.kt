package com.mlesniak.gameboy

import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Basic implementation of the PBM file format.
 * This format supports only black and white, but
 * is sufficient for our needs.
 *
 * See https://en.wikipedia.org/wiki/Netpbm.
 */
class PBM(private val width: Int, private val height: Int) {
    private val mem: Array<Array<Int>> = Array(height) {
        Array(width) { 0 }
    }

    fun write(filename: String) {
        val sb = StringBuilder()
        sb.append("P1\n")
        sb.append("$width $height\n")
        for (row in mem) {
            for (value in row) {
                sb.append(value)
                sb.append(' ')
            }
            sb.append('\n')
        }
        sb.append("\n")

        Files.writeString(Path.of(filename), sb.toString())
    }

    fun set(x: Int, y: Int) {
        mem[y][x] = 1
    }
}
