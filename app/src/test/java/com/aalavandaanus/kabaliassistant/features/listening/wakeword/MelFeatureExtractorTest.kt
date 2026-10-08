package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MelFeatureExtractorTest {

    private val extractor = MelFeatureExtractor()

    @Test
    fun silenceProducesExpectedShapeAndValidValues() {
        val audio = FloatArray(MelFeatureExtractor.WINDOW_SAMPLES)

        val result = extractor.extract(audio)

        assertEquals(
            MelFeatureExtractor.N_MELS,
            result.size
        )

        result.forEach { melBand ->
            assertEquals(
                MelFeatureExtractor.EXPECTED_FRAMES,
                melBand.size
            )

            melBand.forEach { value ->
                assertFalse(value.isNaN())
                assertFalse(value.isInfinite())

                assertTrue(
                    "Value $value is below 0",
                    value >= 0.0f
                )

                assertTrue(
                    "Value $value is above 1",
                    value <= 1.0f
                )
            }
        }
    }

    @Test
    fun deterministicAudioProducesSameResult() {
        val audio = FloatArray(MelFeatureExtractor.WINDOW_SAMPLES) { index ->
            val t = index.toDouble() / MelFeatureExtractor.SAMPLE_RATE

            (
                    0.5 * kotlin.math.sin(
                        2.0 *
                                kotlin.math.PI *
                                440.0 *
                                t
                    )
                    ).toFloat()
        }

        val first = extractor.extract(audio)
        val second = extractor.extract(audio)

        assertEquals(
            first.size,
            second.size
        )

        for (m in first.indices) {
            assertEquals(
                first[m].size,
                second[m].size
            )

            for (t in first[m].indices) {
                assertEquals(
                    first[m][t],
                    second[m][t],
                    0.000001f
                )
            }
        }
    }
}