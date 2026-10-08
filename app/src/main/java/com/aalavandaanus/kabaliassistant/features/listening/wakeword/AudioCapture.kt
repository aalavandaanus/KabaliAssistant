package com.aalavandaanus.kabaliassistant.features.listening.wakeword

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat

class AudioCapture(
    context: Context
) : AutoCloseable {

    companion object {
        const val SAMPLE_RATE = 16_000

        private const val CHANNEL_CONFIG =
            AudioFormat.CHANNEL_IN_MONO

        private const val AUDIO_FORMAT =
            AudioFormat.ENCODING_PCM_16BIT
    }

    private val context =
        context.applicationContext

    private var audioRecord: AudioRecord? = null

    fun start(
        onSamples: (FloatArray) -> Unit
    ) {
        check(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            "RECORD_AUDIO permission is required."
        }

        check(audioRecord == null) {
            "AudioCapture is already running."
        }

        val minimumBufferSize =
            AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

        require(
            minimumBufferSize > 0
        ) {
            "Unable to determine microphone buffer size."
        }

        val bufferSize =
            maxOf(
                minimumBufferSize,
                SAMPLE_RATE
            )

        val recorder =
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

        check(
            recorder.state == AudioRecord.STATE_INITIALIZED
        ) {
            recorder.release()
            "Unable to initialize microphone."
        }

        audioRecord = recorder

        recorder.startRecording()

        val thread =
            Thread {
                captureLoop(
                    recorder,
                    onSamples
                )
            }

        thread.name = "KabaliAudioCapture"

        thread.start()
    }

    fun stop() {
        val recorder =
            audioRecord
                ?: return

        audioRecord = null

        try {
            if (
                recorder.recordingState ==
                AudioRecord.RECORDSTATE_RECORDING
            ) {
                recorder.stop()
            }
        } finally {
            recorder.release()
        }
    }

    override fun close() {
        stop()
    }

    private fun captureLoop(
        recorder: AudioRecord,
        onSamples: (FloatArray) -> Unit
    ) {
        val buffer =
            ShortArray(1024)

        while (audioRecord === recorder) {

            val count =
                recorder.read(
                    buffer,
                    0,
                    buffer.size
                )

            if (count <= 0) {
                continue
            }

            val samples =
                FloatArray(count)

            for (i in 0 until count) {
                samples[i] =
                    buffer[i] / 32768.0f
            }

            onSamples(samples)
        }
    }
}