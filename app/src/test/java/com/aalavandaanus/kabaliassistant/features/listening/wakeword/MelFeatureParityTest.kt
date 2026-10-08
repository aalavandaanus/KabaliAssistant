package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class MelFeatureParityTest {

    @Test
    fun androidMelMatchesPythonReference() {
        val audio = FloatArray(MelFeatureExtractor.WINDOW_SAMPLES) { index ->
            val t =
                index.toDouble() /
                        MelFeatureExtractor.SAMPLE_RATE

            (
                    0.50 * sin(
                        2.0 * PI * 440.0 * t
                    ) +
                            0.25 * sin(
                        2.0 * PI * 1000.0 * t
                    ) +
                            0.10 * sin(
                        2.0 * PI * 3000.0 * t
                    )
                    ).toFloat()
        }

        val androidMel =
            MelFeatureExtractor().extract(audio)

        val pythonMel =
            loadPythonReference()

        assertEquals(
            40,
            androidMel.size
        )

        assertEquals(
            201,
            androidMel[0].size
        )

        var maxDifference = 0.0f
        var totalDifference = 0.0

        for (m in 0 until 40) {
            for (t in 0 until 201) {
                val difference =
                    kotlin.math.abs(
                        androidMel[m][t] -
                                pythonMel[m][t]
                    )

                if (difference > maxDifference) {
                    maxDifference = difference
                }

                totalDifference += difference
            }
        }

        val meanDifference =
            totalDifference / (40.0 * 201.0)

        System.err.println("========================================")
        System.err.println("Mel parity comparison")
        System.err.println("Maximum difference: $maxDifference")
        System.err.println("Mean difference:    $meanDifference")
        System.err.println("========================================")

        /*
         * We are deliberately using a relatively loose
         * tolerance for the first parity test.
         *
         * If this fails, we will inspect exactly where
         * the difference comes from rather than guessing.
         */
        assertTrue(
            "Maximum Mel difference was $maxDifference, mean difference was $meanDifference",
            maxDifference < 0.01f
        )
    }

    private fun loadPythonReference(): Array<FloatArray> {
        val stream =
            javaClass.classLoader!!
                .getResourceAsStream("mel_reference.csv")
                ?: error(
                    "mel_reference.csv was not found in test resources"
                )

        return stream.bufferedReader().use { reader ->

            val rows = reader.readLines()

            assertEquals(
                "Python reference must contain 40 rows",
                40,
                rows.size
            )

            Array(40) { rowIndex ->

                val values =
                    rows[rowIndex]
                        .split(",")
                        .map { it.trim().toFloat() }

                assertEquals(
                    "Python reference row $rowIndex must contain 201 values",
                    201,
                    values.size
                )

                FloatArray(201) { columnIndex ->
                    values[columnIndex]
                }
            }
        }
    }
}