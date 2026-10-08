package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession

class WakeWordModel(
    context: Context
) : AutoCloseable {

    companion object {
        private const val MODEL_FILE = "kabali_model.onnx"
        private const val INPUT_NAME = "input"
        private const val OUTPUT_NAME = "output"

        const val NEGATIVE_CLASS = 0
        const val KABALI_CLASS = 1
    }

    private val environment =
        OrtEnvironment.getEnvironment()

    private val session: OrtSession

    init {
        val modelBytes =
            context.applicationContext
                .assets
                .open(MODEL_FILE)
                .use { it.readBytes() }

        session =
            environment.createSession(
                modelBytes,
                OrtSession.SessionOptions()
            )
    }

    fun predict(
        melFeatures: Array<FloatArray>
    ): Prediction {

        require(melFeatures.size == MelFeatureExtractor.N_MELS) {
            "Expected ${MelFeatureExtractor.N_MELS} Mel bands, got ${melFeatures.size}"
        }

        require(
            melFeatures.all {
                it.size == MelFeatureExtractor.EXPECTED_FRAMES
            }
        ) {
            "Expected ${MelFeatureExtractor.EXPECTED_FRAMES} frames per Mel band."
        }

        val input =
            Array(1) {
                Array(1) {
                    Array(MelFeatureExtractor.N_MELS) { melBand ->
                        melFeatures[melBand]
                    }
                }
            }

        OnnxTensor.createTensor(
            environment,
            input
        ).use { tensor ->

            session.run(
                mapOf(
                    INPUT_NAME to tensor
                )
            ).use { results ->

                val outputValue =
                    results[OUTPUT_NAME]
                        .orElseThrow {
                            IllegalStateException(
                                "ONNX output '$OUTPUT_NAME' was not returned."
                            )
                        }

                val output =
                    outputValue.getValue()

                val logits =
                    extractLogits(output)

                val probabilities =
                    softmax(logits)

                return Prediction(
                    negativeProbability =
                        probabilities[NEGATIVE_CLASS],

                    kabaliProbability =
                        probabilities[KABALI_CLASS]
                )
            }
        }
    }

    private fun extractLogits(
        output: Any
    ): FloatArray {

        require(output is Array<*>) {
            "Unexpected ONNX output type: ${output::class.java}"
        }

        require(output.size == 1) {
            "Expected ONNX output batch size 1, got ${output.size}"
        }

        val row =
            output[0]

        require(row is FloatArray) {
            "Expected ONNX output [1,2] as FloatArray."
        }

        require(row.size == 2) {
            "Expected 2 output classes, got ${row.size}"
        }

        return row
    }

    private fun softmax(
        logits: FloatArray
    ): FloatArray {

        val maxLogit =
            logits.maxOrNull()
                ?: error("ONNX model returned no logits.")

        val exponentials =
            FloatArray(logits.size) { index ->
                kotlin.math.exp(
                    (logits[index] - maxLogit).toDouble()
                ).toFloat()
            }

        val sum =
            exponentials.sum()

        return FloatArray(logits.size) { index ->
            exponentials[index] / sum
        }
    }

    override fun close() {
        session.close()
    }

    data class Prediction(
        val negativeProbability: Float,
        val kabaliProbability: Float
    )
}