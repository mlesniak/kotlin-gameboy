package com.mlesniak.gameboy

import kotlin.experimental.and

/**
 * Since we do not want to add more external libraries, we simulate
 * video output by writing out PPM files whenever a change in
 * rendering state occurred.
 *
 * These files can be concatenated via external tools to create
 * a gif or a video. To create a gif from the output, you have
 * to install imagemagick via
 *
 *      brew install imagemagick
 *
 * and execute
 *
 *      convert -delay 1x30 -loop 0 logo-*.ppm logo.gif
 *
 * We receive the complete memory as a parameter and access only
 * the area which is related to the video output, which is stored
 * as tile references in $8000-$97FF (VRAM).
 *
 * VRAM background map referring to tiles is at 9800-9A33.
 */
class Video(private val mem: ByteArray) {
    private val outputDirectory = "frames/"
    private val width = 160
    private val height = 144

    // Position of the VRAM window upper and left
    // corner in memory.
    private val scy = 0xFF42
    private val scx = 0xFF43
    private var counter= 0
    private var oldy: Byte = 0

    /**
     * Render video using indexed tile map at 0x9800. Take
     * each byte times 0x10 (since every tile contains 0x10
     * bytes) and add 0x8000 (since LCDC, bit 4 in 0xFF40
     * is set to 1). Parse byte accordingly and render it.
     *
     * We split rendering into two parts: the whole background
     * map is 256x256, and we render the tilemap into a virtual
     * framebuffer first. Afterwards, we use the 256x256 pixel
     * area and cut out the actual image of dimensions 160x144
     * by taking into account SCY and SCX.
     *
     * DO NOTHING IF SCY DID NOT CHANGE.
     */
    fun render() {
        // Check if we have to do something,
        // i.e. there was a state change affecting
        // video output.
        if (oldy == mem[scy]) {
            return
        }
        oldy = mem[scy]
        counter++

        val framebuffer = renderIntoFramebuffer()
        val filename = "logo-${String.format("%05d", counter)}.ppm"
        renderIntoFile(framebuffer, filename)
    }

    private fun renderIntoFile(framebuffer: Array<Array<Int>>, filename: String) {
        val image = PPM(width, height)
        // Render part of the framebuffer (based on SCX and SCY)
        // to an image. Take care of wrapping around as well (in
        // our case, this is only relevant for SCY, since SCX is
        // constant anyway).
        for (y in 0 until height) {
            for (x in 0 until width) {
                val xr = (mem[scx] + x) % 256
                val yr = (mem[scy] + y) % 256
                if (framebuffer[yr][xr] != 0) {
                    image.set(x, y)
                }
            }
        }
        image.write(outputDirectory + filename)
    }

    private fun renderIntoFramebuffer(): Array<Array<Int>> {
        val framebuffer = Array(256) { Array(256) { 0 } }
        for (y in 0 until 31) {
            for (x in 0 until 31) {
                val addr = (y * 32 + x) + 0x9800
                val tileIndex = mem[addr]

                // For every tile, find the address
                // referring to the actual tile data.
                // '* 0x10' since every tile contains
                // 16 (0x10) bytes, and we index into
                // the whole memory map.
                val tileAddr = 0x8000 + tileIndex * 0x10

                // Write the expanded tile's pixel to
                // the correct position in our framebuffer.
                val pixels = expandTile(tileAddr)
                pixels.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { col, v ->
                        // The color value can be in the range 0..3, but
                        // since we render only monochrome, we treat the
                        // values as 0 or 1.
                        if (v > 0) {
                            val xr = x * 8 + col
                            var yr = y * 8 + rowIndex
                            framebuffer[yr][xr] = v
                        }
                    }
                }
            }
        }
        return framebuffer
    }

    /**
     * Returns a 2D grid interpreting the pixel data
     * according to the VRAM specification.
     *
     * See https://gbdev.io/pandocs/Tile_Data.html for
     * more information on the technical details.
     */
    private fun expandTile(tileAddr: Int): Array<Array<Int>> {
        val res: Array<Array<Int>> = Array(8) {
            Array(8) { 0 }
        }

        // Two bytes make a row. Hence idx == row.
        for (idx in 0..7) {
            var start = tileAddr + idx * 2
            var a = mem[start]
            var b = mem[start + 1]
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
}