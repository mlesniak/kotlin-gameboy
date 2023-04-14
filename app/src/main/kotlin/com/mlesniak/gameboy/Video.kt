package com.mlesniak.gameboy

/**
 * Since we do not want to add more external libraries, we simulate
 * video output by writing out PBM files on a continous basis.
 *
 * These files can be concatenated via external tools to create
 * a gif or a video.
 */
class Video {
    val width = 160
    val height = 144
}