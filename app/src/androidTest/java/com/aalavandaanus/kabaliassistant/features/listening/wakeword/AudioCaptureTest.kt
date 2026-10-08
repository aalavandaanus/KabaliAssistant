package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlin.math.sqrt
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioCaptureTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO
        )

    @Test
    fun microphone_capturesAudioSamples() {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val audioCapture =
            AudioCapture(context)

        val allSamples =
            mutableListOf<Float>()

        try {
            audioCapture.start { samples ->
                synchronized(allSamples) {
                    allSamples.addAll(
                        samples.toList()
                    )
                }
            }

            Thread.sleep(2_500L)

        } finally {
            audioCapture.stop()
        }

        val samples =
            synchronized(allSamples) {
                allSamples.toFloatArray()
            }

        println(
            "Captured samples: ${samples.size}"
        )

        assertTrue(
            "Microphone did not capture enough samples.",
            samples.size >=
                    AudioCapture.SAMPLE_RATE
        )

        var sumSquares = 0.0
        var minimum = Float.MAX_VALUE
        var maximum = -Float.MAX_VALUE

        for (sample in samples) {
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
                        samples.size
            )

        println(
            "Audio RMS: $rms"
        )

        println(
            "Audio minimum: $minimum"
        )

        println(
            "Audio maximum: $maximum"
        )

        assertTrue(
            "Audio RMS is unexpectedly zero.",
            rms > 0.0001
        )
    }
}