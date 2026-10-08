package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioCaptureRollingBufferTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO
        )

    @Test
    fun microphoneFeedsRollingBuffer() {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val audioCapture =
            AudioCapture(context)

        val rollingBuffer =
            RollingAudioBuffer()

        try {
            audioCapture.start { samples ->
                rollingBuffer.add(samples)
            }

            val timeoutAt =
                System.currentTimeMillis() + 5_000L

            while (
                !rollingBuffer.isFull() &&
                System.currentTimeMillis() < timeoutAt
            ) {
                Thread.sleep(50L)
            }

        } finally {
            audioCapture.stop()
        }

        assertTrue(
            "Rolling buffer did not become full.",
            rollingBuffer.isFull()
        )

        assertEquals(
            RollingAudioBuffer.CAPACITY_SAMPLES,
            rollingBuffer.currentSize()
        )

        val window =
            rollingBuffer.getWindow()

        assertNotNull(
            "Rolling buffer did not return a window.",
            window
        )

        requireNotNull(window)

        assertEquals(
            RollingAudioBuffer.CAPACITY_SAMPLES,
            window.size
        )

        var sumSquares = 0.0

        var minimum =
            Float.MAX_VALUE

        var maximum =
            -Float.MAX_VALUE

        for (sample in window) {

            sumSquares +=
                sample.toDouble() *
                        sample.toDouble()

            minimum =
                minOf(
                    minimum,
                    sample
                )

            maximum =
                maxOf(
                    maximum,
                    sample
                )
        }

        val rms =
            sqrt(
                sumSquares /
                        window.size
            )

        println(
            "Rolling buffer samples: ${window.size}"
        )

        println(
            "Rolling buffer RMS: $rms"
        )

        println(
            "Rolling buffer minimum: $minimum"
        )

        println(
            "Rolling buffer maximum: $maximum"
        )

        assertTrue(
            "Rolling buffer audio RMS is unexpectedly zero.",
            rms > 0.0001
        )
    }
}