package com.mlesniak.gameboy

import com.mlesniak.gameboy.debug.hex

/**
 * Since we do not want to add more external libraries, we simulate
 * video output by writing out PBM files on a continous basis.
 *
 * These files can be concatenated via external tools to create
 * a gif or a video.
 *
 * We receive the complete memory as a parameter and access only
 * the area which is related to the video output.
 */
// TODO(mlesniak) How often should we write a new file?
class Video(val mem: ByteArray) {
    val width = 160
    val height = 144

    // Position of the VRAM window upper and left corner.
    val scy = 0xFF42
    val scx = 0xFF43

    var oldx: Byte = 0
    var oldy: Byte = 0
    fun tick() {
        if (oldx != mem[scx]) {
            oldx = mem[scx]
            println("scx=${oldx.hex(2)}")
        }
        if (oldy != mem[scy]) {
            oldy = mem[scy]
            println("scy=${oldy.hex(2)}")
        }
    }
}