package com.mlesniak.gameboy

import com.mlesniak.gameboy.debug.Debug
import com.mlesniak.gameboy.debug.hex
import kotlin.experimental.and

/**
 * Since we do not want to add more external libraries, we simulate
 * video output by writing out PBM files on a continous basis.
 *
 * These files can be concatenated via external tools to create
 * a gif or a video.
 *
 * We receive the complete memory as a parameter and access only
 * the area which is related to the video output, which is stored
 * as tile references in $8000-$97FF (VRAM).
 *
 * VRAM background map referring to tiles is at 9800-9A33.
 * 20 tiles per row. -> 20 bytes per row.
 */
// TODO(mlesniak) How often should we write a new file?
class Video(val mem: ByteArray) {
    val width = 160
    val height = 144

    // Position of the VRAM window upper and left corner.
    private val scy = 0xFF42
    private val scx = 0xFF43

    var oldx: Byte = 0
    var oldy: Byte = 0
    fun tick() {
        // if (oldx != mem[scx]) {
        //     oldx = mem[scx]
        //     println("scx=${oldx.hex(2)}")
        // }
        // if (oldy != mem[scy]) {
        //     oldy = mem[scy]
        //     println("scy=${oldy.hex(2)}")
        // }
    }

    /**
     * Render video using indexed tile map at 0x9800. Take
     * each byte times 0x10 (since every tile contains 0x10
     * bytes) and add 0x8000 (since LCDC, bit 4 in 0xFF40
     * is set to 1). Parse byte accordingly and render it.
     */
    fun render() {
        // val image = PBM(256, 256)
        val image = PBM(width, height)

        for (y in 0 until 31) {
            for (x in 0 until 31) {
                // While memory allows for 256x256 pixel
                // images, the Gameboy supports only 160x144.
                // Stop before rendering more.
                if (y * 8 >= height || x * 8 >= width) {
                    continue
                }

                val addr = (y * 32 + x) + 0x9800
                val tileIndex = mem[addr]
                if (tileIndex == 0x00.toByte()) {
                    // TODO(mlesniak) remove this if later.
                    continue
                }
                println(" ${tileIndex.hex(2)}")

                // For every tile, find the address
                // referring to the actual tile data.
                // '* 0x10' since every tile contains
                // of 16 (0x10) bytes, and we index
                // into the whole memory map.
                val tileAddr = 0x8000 + tileIndex * 0x10
                Debug.hexdump(mem, tileAddr..tileAddr + 0x10)

                val pixels = toPixel(tileAddr)
                pixels.forEach { row ->
                    row.forEach { v ->
                        if (v > 0) {
                            print("#")
                        } else {
                            print(" ")
                        }
                    }
                    println()
                }
                pixels.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { col, v ->
                        // The array value can be in the range 0..3, but
                        // since we render only monochrome, we treat the
                        // values as 0 or 1.
                        if (v > 0) {
                            val xr = x * 8 + col
                            val yr = y * 8 + rowIndex
                            image.set(xr, yr)
                        }
                    }
                }
            }
        }
        image.write("logo.pbm")

        Debug.hexdump(mem, 0x0104..0x0200)
        Debug.hexdump(mem, 0x8010..0x8020)
        println("trademark")
        val i = 0x8000 + 0x18 * 0x10
        Debug.hexdump(mem, i..i + 0x20)
    }

    /**
     * Returns a 2D grid interpreting the pixel data
     * according to the VRAM specification.
     *
     * See https://gbdev.io/pandocs/Tile_Data.html for
     * more information on the technical details.
     */
    private fun toPixel(tileAddr: Int): Array<Array<Int>> {
        val res: Array<Array<Int>> = Array(8) {
            Array(8) { 0 }
        }

        // Two bytes make a row. Hence idx == row.
        for (idx in 0..7) {
            var start = tileAddr + idx * 2
            var a = mem[start]
            var b = mem[start + 1]
            println("a=${a.hex(2)} b=${b.hex(2)}")
            for (bit in 7 downTo 0) {
                val tmp = (1 shl bit).toByte()
                val aset = (a and tmp) != 0x00.toByte()
                val bset = (b and tmp) != 0x00.toByte()
                val n = if (bset) 2 else 0 + if (aset) 1 else 0
                res[idx][7-bit] = n
            }
        }

        return res
    }

    /**
     * Hashes VRAM portion of memory which allows us to
     * check if there were any modification done via the
     * CPU instructions.
     */
    private fun hashVRAM(): Int =
        mem.slice(0x8000..0x97FF).hashCode()

    // 8192 bytes in VRAM

}