package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.PI
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WakeWordModelTest {

    @Test
    fun deterministicAudio_matchesPythonOnnxReference() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val audio =
            createTestAudio()

        val extractor =
            MelFeatureExtractor()

        val melFeatures =
            extractor.extract(audio)

        val model =
            WakeWordModel(context)

        model.use {
            val prediction =
                it.predict(melFeatures)

            println(
                "Android negative probability = " +
                        prediction.negativeProbability
            )

            println(
                "Android Kabali probability = " +
                        prediction.kabaliProbability
            )

            /*
             * Python reference:
             *
             * Negative = 0.5413216352
             * Kabali   = 0.4586783946
             */

            assertEquals(
                0.5413216352f,
                prediction.negativeProbability,
                0.01f
            )

            assertEquals(
                0.4586783946f,
                prediction.kabaliProbability,
                0.01f
            )

            assertTrue(
                "Deterministic test audio should be classified as NEGATIVE.",
                prediction.kabaliProbability < 0.50f
            )
        }
    }

    private fun createTestAudio(): FloatArray {

        val sampleRate =
            MelFeatureExtractor.SAMPLE_RATE

        val sampleCount =
            MelFeatureExtractor.WINDOW_SAMPLES

        return FloatArray(sampleCount) { index ->

            val t =
                index.toDouble() /
                        sampleRate.toDouble()

            (
                    0.50 *
                            sin(
                                2.0 *
                                        PI *
                                        440.0 *
                                        t
                            )
                            +
                            0.25 *
                            sin(
                                2.0 *
                                        PI *
                                        1000.0 *
                                        t
                            )
                            +
                            0.10 *
                            sin(
                                2.0 *
                                        PI *
                                        3000.0 *
                                        t
                            )
                    ).toFloat()
        }
    }
}