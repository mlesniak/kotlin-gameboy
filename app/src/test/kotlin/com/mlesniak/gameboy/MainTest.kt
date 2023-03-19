package com.mlesniak.gameboy

import com.mlesniak.gameboy.debug.testBit
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun `test bit`() {
        val b = 0b10000000.toByte()
        assertTrue(b.testBit(7))
        assertFalse(b.testBit(6))
    }
}
