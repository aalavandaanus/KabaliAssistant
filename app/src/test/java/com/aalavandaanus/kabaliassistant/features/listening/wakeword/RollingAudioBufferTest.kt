package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RollingAudioBufferTest {

    @Test
    fun bufferIsNotFullUntilItHasTwoSeconds() {
        val buffer = RollingAudioBuffer()

        buffer.add(FloatArray(10_000) { it.toFloat() })

        assertEquals(
            10_000,
            buffer.currentSize()
        )

        assertFalse(
            buffer.isFull()
        )

        assertNull(
            buffer.getWindow()
        )
    }

    @Test
    fun bufferReturnsExactlyTwoSecondsWhenFull() {
        val buffer = RollingAudioBuffer()

        val samples =
            FloatArray(
                RollingAudioBuffer.CAPACITY_SAMPLES
            ) { it.toFloat() }

        buffer.add(samples)

        assertEquals(
            RollingAudioBuffer.CAPACITY_SAMPLES,
            buffer.currentSize()
        )

        assertTrue(
            buffer.isFull()
        )

        val window =
            buffer.getWindow()

        requireNotNull(window)

        assertEquals(
            RollingAudioBuffer.CAPACITY_SAMPLES,
            window.size
        )

        assertArrayEquals(
            samples,
            window,
            0.0f
        )
    }

    @Test
    fun bufferKeepsNewestSamplesWhenItOverflows() {
        val buffer = RollingAudioBuffer()

        val first =
            FloatArray(32_000) { 1.0f }

        val second =
            FloatArray(1_000) { 2.0f }

        buffer.add(first)
        buffer.add(second)

        val window =
            buffer.getWindow()

        requireNotNull(window)

        assertEquals(
            32_000,
            window.size
        )

        for (i in 0 until 31_000) {
            assertEquals(
                1.0f,
                window[i],
                0.0f
            )
        }

        for (i in 31_000 until 32_000) {
            assertEquals(
                2.0f,
                window[i],
                0.0f
            )
        }
    }

    @Test
    fun bufferCanHandleChunkLargerThanCapacity() {
        val buffer = RollingAudioBuffer()

        val samples =
            FloatArray(40_000) { it.toFloat() }

        buffer.add(samples)

        val window =
            buffer.getWindow()

        requireNotNull(window)

        assertEquals(
            32_000,
            window.size
        )

        assertEquals(
            8_000.0f,
            window.first(),
            0.0f
        )

        assertEquals(
            39_999.0f,
            window.last(),
            0.0f
        )
    }

    @Test
    fun clearEmptiesBuffer() {
        val buffer = RollingAudioBuffer()

        buffer.add(
            FloatArray(32_000)
        )

        assertTrue(
            buffer.isFull()
        )

        buffer.clear()

        assertEquals(
            0,
            buffer.currentSize()
        )

        assertFalse(
            buffer.isFull()
        )

        assertNull(
            buffer.getWindow()
        )
    }
}