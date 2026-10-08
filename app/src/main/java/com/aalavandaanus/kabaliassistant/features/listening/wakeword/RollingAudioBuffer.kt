package com.aalavandaanus.kabaliassistant.features.listening.wakeword

class RollingAudioBuffer {

    companion object {
        const val CAPACITY_SAMPLES =
            MelFeatureExtractor.WINDOW_SAMPLES
    }

    private val buffer =
        FloatArray(CAPACITY_SAMPLES)

    private var size = 0

    @Synchronized
    fun add(samples: FloatArray) {
        if (samples.isEmpty()) {
            return
        }

        if (samples.size >= CAPACITY_SAMPLES) {
            System.arraycopy(
                samples,
                samples.size - CAPACITY_SAMPLES,
                buffer,
                0,
                CAPACITY_SAMPLES
            )

            size = CAPACITY_SAMPLES
            return
        }

        val newSize =
            size + samples.size

        if (newSize <= CAPACITY_SAMPLES) {
            System.arraycopy(
                samples,
                0,
                buffer,
                size,
                samples.size
            )

            size = newSize
            return
        }

        val samplesToDiscard =
            newSize - CAPACITY_SAMPLES

        System.arraycopy(
            buffer,
            samplesToDiscard,
            buffer,
            0,
            size - samplesToDiscard
        )

        System.arraycopy(
            samples,
            0,
            buffer,
            size - samplesToDiscard,
            samples.size
        )

        size = CAPACITY_SAMPLES
    }

    @Synchronized
    fun isFull(): Boolean {
        return size == CAPACITY_SAMPLES
    }

    @Synchronized
    fun currentSize(): Int {
        return size
    }

    @Synchronized
    fun getWindow(): FloatArray? {
        if (!isFull()) {
            return null
        }

        return buffer.copyOf()
    }

    @Synchronized
    fun clear() {
        buffer.fill(0f)
        size = 0
    }
}