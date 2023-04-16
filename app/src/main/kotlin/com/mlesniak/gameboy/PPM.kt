package com.mlesniak.gameboy

import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Basic implementation of the PPM file format.
 * This format supports arbitrary colors, but we
 * set some default colors for the background which
 * is sufficient for our needs.
 *
 * See https://en.wikipedia.org/wiki/Netpbm.
 */
class PPM(private val width: Int, private val height: Int) {
    private val mem: Array<Array<Int>> =
        Array(height) {
            Array(width) { 0 }
        }

    fun write(filename: String) {
        val sb = StringBuilder()
        sb.append("P3 $width $height 255\n")
        for (row in mem) {
            for (value in row) {
                if (value == 0) {
                    // Green-ish background color.
                    sb.append(" 222 249 208")
                } else {
                    // Everything else is pure black.
                    sb.append(" 0 0 0")
                }
            }
            sb.append('\n')
        }
        sb.append('\n')
        Files.writeString(Path.of(filename), sb.toString())
    }

    fun set(x: Int, y: Int) {
        mem[y][x] = 1
    }
}