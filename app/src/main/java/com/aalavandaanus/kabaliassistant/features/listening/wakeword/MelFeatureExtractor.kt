package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin

/**
 * Converts a 2-second, 16 kHz mono PCM audio window into the
 * Mel-spectrogram expected by kabali_model.onnx.
 *
 * Python reference:
 *
 * librosa.feature.melspectrogram(
 *     y=audio,
 *     sr=16000,
 *     n_fft=512,
 *     hop_length=160,
 *     n_mels=40,
 *     center=True,
 *     power=2.0
 * )
 *
 * mel_db = librosa.power_to_db(mel, ref=np.max)
 *
 * mel_norm = (mel_db + 20.0) / 80.0
 * mel_norm = np.clip(mel_norm, 0.0, 1.0)
 *
 * Expected output:
 *     [40][201]
 */
class MelFeatureExtractor {

    companion object {
        const val SAMPLE_RATE = 16_000
        const val WINDOW_SAMPLES = 32_000

        const val N_MELS = 40
        const val N_FFT = 512
        const val HOP_LENGTH = 160

        const val EXPECTED_FRAMES = 201

        private const val MIN_POWER = 1e-10
        private const val DB_FLOOR = -80.0
    }

    private val hannWindow = createHannWindow()
    private val melFilterBank = createMelFilterBank()

    fun extract(audio: FloatArray): Array<FloatArray> {
        require(audio.size == WINDOW_SAMPLES) {
            "Expected $WINDOW_SAMPLES samples, got ${audio.size}"
        }

        val padded = centerPad(audio)

        val frameCount =
            1 + (padded.size - N_FFT) / HOP_LENGTH

        require(frameCount == EXPECTED_FRAMES) {
            "Expected $EXPECTED_FRAMES frames, got $frameCount"
        }

        val melPower = Array(N_MELS) {
            FloatArray(frameCount)
        }

        val real = FloatArray(N_FFT)
        val imag = FloatArray(N_FFT)
        val powerSpectrum = FloatArray(N_FFT / 2 + 1)

        for (frame in 0 until frameCount) {
            val start = frame * HOP_LENGTH

            for (i in 0 until N_FFT) {
                real[i] = padded[start + i] * hannWindow[i]
                imag[i] = 0f
            }

            fft(real, imag)

            for (k in 0..N_FFT / 2) {
                val r = real[k]
                val im = imag[k]

                powerSpectrum[k] =
                    r * r + im * im
            }

            for (melIndex in 0 until N_MELS) {
                var energy = 0.0

                val filter =
                    melFilterBank[melIndex]

                for (k in powerSpectrum.indices) {
                    energy +=
                        powerSpectrum[k].toDouble() *
                                filter[k].toDouble()
                }

                melPower[melIndex][frame] =
                    energy.toFloat()
            }
        }

        return powerToNormalizedDb(melPower)
    }

    private fun centerPad(
        audio: FloatArray
    ): FloatArray {
        val pad = N_FFT / 2

        val padded =
            FloatArray(
                audio.size + pad * 2
            )

        System.arraycopy(
            audio,
            0,
            padded,
            pad,
            audio.size
        )

        return padded
    }

    private fun powerToNormalizedDb(
        melPower: Array<FloatArray>
    ): Array<FloatArray> {

        var globalMaximum = MIN_POWER

        for (melBand in melPower) {
            for (power in melBand) {
                val value = power.toDouble()

                if (value > globalMaximum) {
                    globalMaximum = value
                }
            }
        }

        val normalized =
            Array(N_MELS) {
                FloatArray(EXPECTED_FRAMES)
            }

        for (m in 0 until N_MELS) {
            for (t in 0 until EXPECTED_FRAMES) {

                val power =
                    maxOf(
                        melPower[m][t].toDouble(),
                        MIN_POWER
                    )

                var db =
                    10.0 *
                            log10(
                                power /
                                        globalMaximum
                            )

                db =
                    maxOf(
                        db,
                        DB_FLOOR
                    )

                var value =
                    (db + 20.0) / 80.0

                value =
                    value.coerceIn(
                        0.0,
                        1.0
                    )

                normalized[m][t] =
                    value.toFloat()
            }
        }

        return normalized
    }

    private fun createHannWindow(): FloatArray {
        val window =
            FloatArray(N_FFT)

        for (i in 0 until N_FFT) {
            window[i] =
                (
                        0.5 -
                                0.5 *
                                cos(
                                    2.0 *
                                            PI *
                                            i /
                                            N_FFT
                                )
                        ).toFloat()
        }

        return window
    }

    /**
     * Creates the librosa-compatible Mel filter bank.
     *
     * Important:
     *
     * librosa does NOT first round the Mel frequencies to
     * integer FFT bins. It evaluates the triangular filters
     * against the actual FFT-bin frequencies.
     *
     * This is important for feature parity with the Python
     * training pipeline.
     */
    private fun createMelFilterBank(): Array<FloatArray> {

        val filterBank =
            Array(N_MELS) {
                FloatArray(N_FFT / 2 + 1)
            }

        val fMin = 0.0
        val fMax = SAMPLE_RATE / 2.0

        val melMin =
            hzToMel(fMin)

        val melMax =
            hzToMel(fMax)

        val melF =
            DoubleArray(N_MELS + 2)

        for (i in melF.indices) {
            melF[i] =
                melMin +
                        (
                                melMax - melMin
                                ) *
                        i /
                        (N_MELS + 1)
        }

        val hzF =
            DoubleArray(N_MELS + 2)

        for (i in hzF.indices) {
            hzF[i] =
                melToHz(melF[i])
        }

        /*
         * Equivalent to librosa.fft_frequencies():
         *
         *     k * sr / n_fft
         */
        val fftFrequencies =
            DoubleArray(
                N_FFT / 2 + 1
            )

        for (k in fftFrequencies.indices) {
            fftFrequencies[k] =
                k.toDouble() *
                        SAMPLE_RATE.toDouble() /
                        N_FFT.toDouble()
        }

        for (m in 0 until N_MELS) {

            val lowerFrequency =
                hzF[m]

            val centerFrequency =
                hzF[m + 1]

            val upperFrequency =
                hzF[m + 2]

            val lowerWidth =
                centerFrequency -
                        lowerFrequency

            val upperWidth =
                upperFrequency -
                        centerFrequency

            for (k in fftFrequencies.indices) {

                val frequency =
                    fftFrequencies[k]

                val lower =
                    if (lowerWidth > 0.0) {
                        (
                                frequency -
                                        lowerFrequency
                                ) /
                                lowerWidth
                    } else {
                        0.0
                    }

                val upper =
                    if (upperWidth > 0.0) {
                        (
                                upperFrequency -
                                        frequency
                                ) /
                                upperWidth
                    } else {
                        0.0
                    }

                val triangularWeight =
                    maxOf(
                        0.0,
                        minOf(
                            lower,
                            upper
                        )
                    )

                /*
                 * Slaney normalization:
                 *
                 *     2 / (f[m+2] - f[m])
                 */
                val enorm =
                    2.0 /
                            (
                                    upperFrequency -
                                            lowerFrequency
                                    )

                filterBank[m][k] =
                    (
                            triangularWeight *
                                    enorm
                            ).toFloat()
            }
        }

        return filterBank
    }

    /**
     * Slaney's Mel scale, which is librosa's default
     * when htk=false.
     */
    private fun hzToMel(
        hz: Double
    ): Double {

        val fSp =
            200.0 / 3.0

        if (hz < 1000.0) {
            return hz / fSp
        }

        val minLogHz =
            1000.0

        val minLogMel =
            minLogHz / fSp

        val logStep =
            ln(6.4) / 27.0

        return minLogMel +
                ln(hz / minLogHz) /
                logStep
    }

    private fun melToHz(
        mel: Double
    ): Double {

        val fSp =
            200.0 / 3.0

        val minLogHz =
            1000.0

        val minLogMel =
            minLogHz / fSp

        val logStep =
            ln(6.4) / 27.0

        if (mel < minLogMel) {
            return mel * fSp
        }

        return minLogHz *
                exp(
                    logStep *
                            (mel - minLogMel)
                )
    }

    private fun fft(
        real: FloatArray,
        imag: FloatArray
    ) {
        val n = real.size

        var j = 0

        for (i in 1 until n) {

            var bit =
                n shr 1

            while ((j and bit) != 0) {
                j =
                    j xor bit

                bit =
                    bit shr 1
            }

            j =
                j xor bit

            if (i < j) {

                val tempReal =
                    real[i]

                real[i] =
                    real[j]

                real[j] =
                    tempReal

                val tempImag =
                    imag[i]

                imag[i] =
                    imag[j]

                imag[j] =
                    tempImag
            }
        }

        var length = 2

        while (length <= n) {

            val angle =
                -2.0 *
                        PI /
                        length

            val wLenReal =
                cos(angle)

            val wLenImag =
                sin(angle)

            var i = 0

            while (i < n) {

                var wReal = 1.0
                var wImag = 0.0

                for (j2 in 0 until length / 2) {

                    val evenIndex =
                        i + j2

                    val oddIndex =
                        i +
                                j2 +
                                length / 2

                    val uReal =
                        real[evenIndex]

                    val uImag =
                        imag[evenIndex]

                    val vReal =
                        real[oddIndex] *
                                wReal -
                                imag[oddIndex] *
                                wImag

                    val vImag =
                        real[oddIndex] *
                                wImag +
                                imag[oddIndex] *
                                wReal

                    real[evenIndex] =
                        (
                                uReal +
                                        vReal
                                ).toFloat()

                    imag[evenIndex] =
                        (
                                uImag +
                                        vImag
                                ).toFloat()

                    real[oddIndex] =
                        (
                                uReal -
                                        vReal
                                ).toFloat()

                    imag[oddIndex] =
                        (
                                uImag -
                                        vImag
                                ).toFloat()

                    val nextWReal =
                        wReal *
                                wLenReal -
                                wImag *
                                wLenImag

                    wImag =
                        wReal *
                                wLenImag +
                                wImag *
                                wLenReal

                    wReal =
                        nextWReal
                }

                i += length
            }

            length =
                length shl 1
        }
    }
}