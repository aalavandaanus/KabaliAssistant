package com.aalavandaanus.kabaliassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import android.os.Handler
import android.os.Looper
import java.io.FileOutputStream
import java.io.RandomAccessFile
import com.aalavandaanus.kabaliassistant.navigation.AppNavigation
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayWorkScheduler

class MainActivity : ComponentActivity() {

    private val tag = "KABALI"
    private val mainHandler =
        Handler(Looper.getMainLooper())
    // ============================================================
    // MODEL
    // ============================================================

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    // ============================================================
    // AUDIO / MODEL PARAMETERS
    // ============================================================

    private val sampleRate = 16000
    private val numSamples = 32000

    private val nMels = 40
    private val nFft = 512
    private val hopLength = 160

    private val rmsThreshold = 0.005f

    // Start conservative enough to avoid too many false triggers.
    private val kabaliThreshold = 0.50f

    // ============================================================
    // AUDIO RECORD
    // ============================================================

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var diagnosticWavFile: File? = null
    private var diagnosticWavOutput: FileOutputStream? = null
    private var diagnosticWavDataSize: Int = 0

    @Volatile
    private var isListening = false

    // ============================================================
    // ASSISTANT STATE
    // ============================================================

    private enum class AssistantState {
        IDLE,
        LISTENING_FOR_KABALI,
        LISTENING_FOR_COMMAND,
        PROCESSING_COMMAND
    }

    @Volatile
    private var assistantState =
        AssistantState.IDLE

    // Prevent duplicate wake-word processing.
    @Volatile
    private var wakeWordTriggered = false
    private val diagnosticMode = true
    private val MIN_RMS = 0.005f

    // ============================================================
    // SPEECH RECOGNIZER
    // ============================================================

    private var speechRecognizer: SpeechRecognizer? = null

    // ============================================================
    // MICROPHONE PERMISSION
    // ============================================================

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                Log.d(
                    tag,
                    "MICROPHONE PERMISSION: GRANTED"
                )

            } else {

                Log.d(
                    tag,
                    "MICROPHONE PERMISSION: DENIED"
                )
            }
        }

    // ============================================================
    // ACTIVITY
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState


        )
        BirthdayWorkScheduler.schedule(this)
        setContent {

           // KabaliScreen(
              //  onStartListening = {
                //    startAssistant()
               // },

              //  onVoiceCommand = {
               // //    startSpeechRecognition()
               // },

                //onStop = {
                //  stopAssistant()
             //   }
                        AppNavigation()
           // )
        }

        loadKabaliModel()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    // ============================================================
    // START ASSISTANT
    // ============================================================

    private fun startAssistant() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                tag,
                "Microphone permission required"
            )

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )

            return
        }

        if (ortSession == null) {

            Log.e(
                tag,
                "ONNX model is not loaded"
            )

            return
        }

        wakeWordTriggered = false

        assistantState =
            AssistantState.LISTENING_FOR_KABALI

        startMicrophone()
    }

    // ============================================================
    // STOP ASSISTANT
    // ============================================================

    private fun stopAssistant() {

        Log.d(
            tag,
            "Stopping Kabali Assistant"
        )

        assistantState =
            AssistantState.IDLE

        wakeWordTriggered = false

        stopMicrophone()

        speechRecognizer?.cancel()
        speechRecognizer?.destroy()

        speechRecognizer = null
    }

    // ============================================================
    // MODEL LOADING
    // ============================================================

    private fun loadKabaliModel() {

        try {

            Log.d(
                tag,
                "================================"
            )

            Log.d(
                tag,
                "LOADING KABALI ONNX MODEL"
            )

            Log.d(
                tag,
                "================================"
            )

            val modelFile =
                File(
                    filesDir,
                    "kabali_model.onnx"
                )

            val dataFile =
                File(
                    filesDir,
                    "kabali_model.onnx.data"
                )

            assets.open(
                "kabali_model.onnx"
            ).use { input ->

                modelFile.outputStream().use { output ->

                    input.copyTo(output)
                }
            }

            assets.open(
                "kabali_model.onnx.data"
            ).use { input ->

                dataFile.outputStream().use { output ->

                    input.copyTo(output)
                }
            }

            Log.d(
                tag,
                "Model path: ${modelFile.absolutePath}"
            )

            Log.d(
                tag,
                "Model size: ${modelFile.length()} bytes"
            )

            Log.d(
                tag,
                "Data path: ${dataFile.absolutePath}"
            )

            Log.d(
                tag,
                "Data size: ${dataFile.length()} bytes"
            )

            ortEnvironment =
                OrtEnvironment.getEnvironment()

            ortSession =
                ortEnvironment!!.createSession(
                    modelFile.absolutePath
                )

            Log.d(
                tag,
                "================================"
            )

            Log.d(
                tag,
                "KABALI MODEL LOADED"
            )

            Log.d(
                tag,
                "Input names: ${ortSession!!.inputNames}"
            )

            Log.d(
                tag,
                "Output names: ${ortSession!!.outputNames}"
            )

            Log.d(
                tag,
                "================================"
            )

        } catch (e: Exception) {

            Log.e(
                tag,
                "FAILED TO LOAD KABALI MODEL",
                e
            )
        }
    }
    private fun startDiagnosticRecording() {

        val file = File(filesDir, "kabali_live_test.wav")

        diagnosticWavFile = file
        diagnosticWavOutput = FileOutputStream(file)
        diagnosticWavDataSize = 0

        // Write placeholder WAV header.
        diagnosticWavOutput?.write(ByteArray(44))

        Log.d(
            tag,
            "🎙️ Diagnostic WAV recording started: ${file.absolutePath}"
        )
    }

    private fun writeDiagnosticAudio(
        buffer: ShortArray,
        length: Int
    ) {
        val output = diagnosticWavOutput ?: return

        val bytes = ByteArray(length * 2)

        for (i in 0 until length) {

            val sample = buffer[i].toInt()

            bytes[i * 2] =
                (sample and 0xFF).toByte()

            bytes[i * 2 + 1] =
                ((sample shr 8) and 0xFF).toByte()
        }

        output.write(bytes)

        diagnosticWavDataSize += bytes.size
    }

    private fun stopDiagnosticRecording() {

        val output = diagnosticWavOutput ?: return
        val file = diagnosticWavFile ?: return

        output.flush()
        output.close()

        diagnosticWavOutput = null

        writeWavHeader(
            file = file,
            dataSize = diagnosticWavDataSize
        )

        Log.d(
            tag,
            "🎙️ Diagnostic WAV saved: ${file.absolutePath}"
        )

        Log.d(
            tag,
            "WAV data size: $diagnosticWavDataSize bytes"
        )
    }

    private fun writeWavHeader(
        file: File,
        dataSize: Int
    ) {

        val sampleRate = 16000
        val channels = 1
        val bitsPerSample = 16

        val byteRate =
            sampleRate * channels * bitsPerSample / 8

        val blockAlign =
            channels * bitsPerSample / 8

        val totalFileSize =
            36 + dataSize

        RandomAccessFile(file, "rw").use { raf ->

            raf.seek(0)

            raf.writeBytes("RIFF")
            writeIntLE(raf, totalFileSize)

            raf.writeBytes("WAVE")

            raf.writeBytes("fmt ")
            writeIntLE(raf, 16)

            writeShortLE(raf, 1) // PCM
            writeShortLE(raf, channels)

            writeIntLE(raf, sampleRate)
            writeIntLE(raf, byteRate)

            writeShortLE(raf, blockAlign)
            writeShortLE(raf, bitsPerSample)

            raf.writeBytes("data")
            writeIntLE(raf, dataSize)
        }
    }

    private fun writeIntLE(
        raf: RandomAccessFile,
        value: Int
    ) {
        raf.write(
            byteArrayOf(
                (value and 0xFF).toByte(),
                ((value shr 8) and 0xFF).toByte(),
                ((value shr 16) and 0xFF).toByte(),
                ((value shr 24) and 0xFF).toByte()
            )
        )
    }

    private fun writeShortLE(
        raf: RandomAccessFile,
        value: Int
    ) {
        raf.write(
            byteArrayOf(
                (value and 0xFF).toByte(),
                ((value shr 8) and 0xFF).toByte()
            )
        )
    }
    // ============================================================
    // AUDIO RECORD
    // ============================================================

    private fun startMicrophone() {

        if (isListening) {

            Log.d(
                tag,
                "AudioRecord already running"
            )

            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.e(
                tag,
                "RECORD_AUDIO permission not granted"
            )

            return
        }

        val channelConfig =
            AudioFormat.CHANNEL_IN_MONO

        val audioFormat =
            AudioFormat.ENCODING_PCM_16BIT

        val minBufferSize =
            AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat
            )

        val bufferSize =
            maxOf(
                minBufferSize,
                sampleRate * 2
            )

        audioRecord =
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

        audioRecord!!.startRecording()
        startDiagnosticRecording()

        isListening = true

        assistantState =
            AssistantState.LISTENING_FOR_KABALI

        Log.d(
            tag,
            "================================"
        )

        Log.d(
            tag,
            "🎤 KABALI ASSISTANT LISTENING"
        )

        Log.d(
            tag,
            "Say: Kabali"
        )

        Log.d(
            tag,
            "================================"
        )

        recordingThread =
            thread(
                start = true,
                name = "KabaliAudioThread"
            ) {

                runLiveDetection()
            }
    }

    // ============================================================
    // LIVE DETECTION
    // ============================================================

    private fun runLiveDetection() {

        val window =
            FloatArray(numSamples)

        val pcmBuffer =
            ShortArray(1600)

        var position = 0

        while (isListening) {

            val count =
                audioRecord?.read(
                    pcmBuffer,
                    0,
                    pcmBuffer.size
                ) ?: -1

            if (count <= 0) {

                Log.e(
                    tag,
                    "AudioRecord read failed: $count"
                )

                break
            }

            if (count > 0) {
                writeDiagnosticAudio(pcmBuffer, count)
            }

            for (i in 0 until count) {

                if (!isListening) {
                    break
                }

                window[position] =
                    pcmBuffer[i] /
                            32768.0f

                position++

                if (
                    position >=
                    numSamples
                ) {

                    processLiveWindow(
                        window
                    )

                    position = 0

                    // If Kabali was detected,
                    // processLiveWindow() will
                    // change the state and stop
                    // this detector.
                    if (!isListening) {
                        break
                    }
                }
            }
        }

        Log.d(
            tag,
            "Kabali audio thread stopped"
        )
    }

    // ============================================================
    // PROCESS 2-SECOND WINDOW
    // ============================================================

    private fun processLiveWindow(audio: FloatArray) {

        val rms = calculateRms(audio)

        Log.d(tag, "================================")
        Log.d(tag, "LIVE WINDOW RMS=$rms")

        // Silence / very quiet audio
        if (rms < MIN_RMS) {
            Log.d(tag, "QUIET - skipped")
            return
        }

        Log.d(tag, "Running Kabali model...")

        val mel = createMelSpectrogram(audio)

        Log.d(
            tag,
            "STFT frames: ${mel[0].size}"
        )

        val inputData = Array(1) {
            Array(1) {
                Array(40) { melRow ->
                    FloatArray(201) { frame ->
                        mel[melRow][frame]
                    }
                }
            }
        }

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            inputData
        )

        try {

            ortSession?.let { session ->

                val result = session.run(
                    mapOf(
                        "input" to inputTensor
                    )
                )

                try {

                    val output =
                        result[0].value as Array<*>

                    val logits =
                        output[0] as FloatArray

                    val probabilities =
                        softmax(logits)

                    val negativeProbability =
                        probabilities[0]

                    val kabaliProbability =
                        probabilities[1]

                    Log.d(
                        tag,
                        "P(Negative)=$negativeProbability"
                    )

                    Log.d(
                        tag,
                        "P(Kabali)=$kabaliProbability"
                    )

                    if (diagnosticMode) {

                        Log.d(
                            tag,
                            "🔬 DIAGNOSTIC ONLY - NO WAKE TRIGGER"
                        )

                    } else {

                        if (
                            kabaliProbability >=
                            kabaliThreshold
                        ) {

                            Log.d(
                                tag,
                                "🔥 KABALI DETECTED!"
                            )

                            wakeWordTriggered = true

                            mainHandler.post {

                                assistantState =
                                    AssistantState.LISTENING_FOR_COMMAND

                                stopMicrophone()

                                mainHandler.postDelayed(
                                    {
                                        startSpeechRecognition()
                                    },
                                    250
                                )
                            }
                        }
                    }

                } finally {

                    result.close()
                }
            }

        } finally {

            inputTensor.close()
        }
    }
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f

        val expValues = FloatArray(logits.size)
        var sum = 0.0

        for (i in logits.indices) {
            expValues[i] = kotlin.math.exp(
                (logits[i] - maxLogit).toDouble()
            ).toFloat()

            sum += expValues[i]
        }

        for (i in expValues.indices) {
            expValues[i] = (expValues[i] / sum).toFloat()
        }

        return expValues
    }
    // ============================================================
    // RMS
    // ============================================================

    private fun calculateRms(
        samples: FloatArray
    ): Float {

        var sum = 0.0

        for (sample in samples) {

            sum +=
                sample.toDouble() *
                        sample.toDouble()
        }

        return sqrt(
            sum / samples.size
        ).toFloat()
    }

    // ============================================================
    // ONNX MODEL
    // ============================================================

    private fun runModel(
        mel: Array<FloatArray>
    ): Float {

        val inputData =
            Array(1) {

                Array(1) {

                    Array(nMels) {

                        mel[it]
                    }
                }
            }

        val inputTensor =
            OnnxTensor.createTensor(
                ortEnvironment,
                inputData
            )

        try {

            val inputs =
                mapOf(
                    "input" to inputTensor
                )

            val result =
                ortSession!!.run(
                    inputs
                )

            try {

                val output =
                    result[0].value
                            as Array<*>

                val logits =
                    output[0]
                            as FloatArray

                val negative =
                    logits[0]

                val kabali =
                    logits[1]

                val maxLogit =
                    maxOf(
                        negative,
                        kabali
                    )

                val negativeExp =
                    exp(
                        (
                                negative -
                                        maxLogit
                                ).toDouble()
                    )

                val kabaliExp =
                    exp(
                        (
                                kabali -
                                        maxLogit
                                ).toDouble()
                    )

                val sum =
                    negativeExp +
                            kabaliExp

                return (
                        kabaliExp / sum
                        ).toFloat()

            } finally {

                result.close()
            }

        } finally {

            inputTensor.close()
        }
    }

    // ============================================================
    // SPEECH RECOGNITION
    // ============================================================

    private fun startSpeechRecognition() {

        if (
            assistantState !=
            AssistantState.LISTENING_FOR_COMMAND
        ) {

            return
        }

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(this)
        ) {

            Log.e(
                tag,
                "Speech recognition unavailable"
            )

            restartKabaliListening()

            return
        }

        Log.d(
            tag,
            "================================"
        )

        Log.d(
            tag,
            "🎙️ LISTENING FOR COMMAND"
        )

        Log.d(
            tag,
            "Say: open YouTube"
        )

        Log.d(
            tag,
            "================================"
        )

        speechRecognizer?.destroy()

        speechRecognizer =
            SpeechRecognizer
                .createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(

            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    Log.d(
                        tag,
                        "READY - Speak your command"
                    )
                }

                override fun onBeginningOfSpeech() {

                    Log.d(
                        tag,
                        "Speech started"
                    )
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {

                    Log.d(
                        tag,
                        "Speech ended"
                    )
                }

                override fun onError(
                    error: Int
                ) {

                    Log.e(
                        tag,
                        "Speech recognition error: $error"
                    )

                    restartKabaliListening()
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer
                                .RESULTS_RECOGNITION
                        )

                    val command =
                        matches?.firstOrNull()

                    Log.d(
                        tag,
                        "COMMAND: $command"
                    )

                    assistantState =
                        AssistantState.PROCESSING_COMMAND

                    handleCommand(
                        command
                    )
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )

        val intent =
            Intent(
                RecognizerIntent
                    .ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent
                        .EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent
                        .LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_LANGUAGE,
                    "en-US"
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_PARTIAL_RESULTS,
                    false
                )
            }

        speechRecognizer?.startListening(
            intent
        )
    }

    // ============================================================
    // COMMAND ROUTER
    // ============================================================

    private fun handleCommand(
        command: String?
    ) {

        if (command == null) {

            Log.d(
                tag,
                "No command received"
            )

            restartKabaliListening()

            return
        }

        val normalized =
            command
                .trim()
                .lowercase()

        Log.d(
            tag,
            "Handling command: $normalized"
        )

        when {

            normalized.contains(
                "open youtube"
            ) -> {

                openYouTube()
            }

            else -> {

                Log.d(
                    tag,
                    "Unknown command: $normalized"
                )

                restartKabaliListening()
            }
        }
    }

    // ============================================================
    // YOUTUBE
    // ============================================================

    private fun openYouTube() {

        Log.d(
            tag,
            "▶️ Opening YouTube..."
        )

        val youtubeIntent =
            packageManager
                .getLaunchIntentForPackage(
                    "com.google.android.youtube"
                )

        if (youtubeIntent != null) {

            youtubeIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(
                youtubeIntent
            )

        } else {

            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.youtube.com"
                    )
                )

            startActivity(
                browserIntent
            )
        }

        // Start listening again after
        // opening the application.
        mainHandler.post {

            Thread.sleep(1000)

            restartKabaliListening()
        }
    }

    // ============================================================
    // RESTART WAKE WORD LISTENER
    // ============================================================

    private fun restartKabaliListening() {

        mainHandler.post {

            if (
                isFinishing ||
                isDestroyed
            ) {

                return@post
            }

            Log.d(
                tag,
                "================================"
            )

            Log.d(
                tag,
                "🔄 RESTARTING KABALI LISTENER"
            )

            Log.d(
                tag,
                "================================"
            )

            speechRecognizer?.cancel()
            speechRecognizer?.destroy()

            speechRecognizer = null

            wakeWordTriggered = false

            assistantState =
                AssistantState.LISTENING_FOR_KABALI

            startMicrophone()
        }
    }

    // ============================================================
    // STOP AUDIO RECORD
    // ============================================================

    private fun stopMicrophone() {

        if (!isListening) {
            return
        }

        Log.d(
            tag,
            "Stopping AudioRecord..."
        )

        isListening = false

        stopDiagnosticRecording()

        try {

            audioRecord?.stop()

        } catch (e: Exception) {

            Log.e(
                tag,
                "AudioRecord stop error",
                e
            )
        }

        audioRecord?.release()

        audioRecord = null

        recordingThread = null

        Log.d(
            tag,
            "AudioRecord stopped"
        )
    }

    // ============================================================
    // MEL SPECTROGRAM
    // ============================================================

    private fun createMelSpectrogram(
        audio: FloatArray
    ): Array<FloatArray> {

        val pad =
            nFft / 2

        val padded =
            FloatArray(
                audio.size +
                        pad * 2
            )

        System.arraycopy(
            audio,
            0,
            padded,
            pad,
            audio.size
        )

        val frameCount =
            1 +
                    (
                            padded.size -
                                    nFft
                            ) /
                    hopLength

        val melFilters =
            createMelFilterBank()

        val mel =
            Array(nMels) {
                FloatArray(
                    frameCount
                )
            }

        val window =
            FloatArray(nFft)

        for (i in 0 until nFft) {

            window[i] =
                (
                        0.5 -
                                0.5 *
                                cos(
                                    2.0 *
                                            PI *
                                            i /
                                            nFft
                                )
                        ).toFloat()
        }

        val real =
            FloatArray(nFft)

        val imag =
            FloatArray(nFft)

        val powerSpectrum =
            FloatArray(
                nFft / 2 + 1
            )

        for (
        frame in 0 until frameCount
        ) {

            val start =
                frame *
                        hopLength

            for (
            i in 0 until nFft
            ) {

                real[i] =
                    padded[
                        start + i
                    ] *
                            window[i]

                imag[i] = 0f
            }

            fft(
                real,
                imag
            )

            for (
            k in 0..nFft / 2
            ) {

                val r =
                    real[k]

                val im =
                    imag[k]

                powerSpectrum[k] =
                    r * r +
                            im * im
            }

            for (
            m in 0 until nMels
            ) {

                var energy =
                    0.0

                for (
                k in 0..nFft / 2
                ) {

                    energy +=
                        powerSpectrum[k] *
                                melFilters[m][k]
                }

                mel[m][frame] =
                    energy.toFloat()
            }
        }

        var maxValue =
            0f

        for (
        m in 0 until nMels
        ) {

            for (
            t in 0 until frameCount
            ) {

                if (
                    mel[m][t] >
                    maxValue
                ) {

                    maxValue =
                        mel[m][t]
                }
            }
        }

        if (
            maxValue <= 0f
        ) {

            maxValue =
                1e-10f
        }

        for (
        m in 0 until nMels
        ) {

            for (
            t in 0 until frameCount
            ) {

                val power =
                    maxOf(
                        mel[m][t],
                        1e-10f
                    )

                var db =
                    (
                            10.0 *
                                    log10(
                                        power.toDouble() /
                                                maxValue.toDouble()
                                    )
                            ).toFloat()

                db =
                    maxOf(
                        db,
                        -80f
                    )

                var scaled =
                    (db + 20f) /
                            80f

                scaled =
                    scaled.coerceIn(
                        0f,
                        1f
                    )

                mel[m][t] =
                    scaled
            }
        }

        return mel
    }

    // ============================================================
    // MEL FILTER BANK
    // ============================================================

    private fun createMelFilterBank():
            Array<FloatArray> {

        val filterBank =
            Array(nMels) {
                FloatArray(
                    nFft / 2 + 1
                )
            }

        val fMin =
            0.0

        val fMax =
            sampleRate / 2.0

        val melMin =
            hzToMel(fMin)

        val melMax =
            hzToMel(fMax)

        val melPoints =
            DoubleArray(
                nMels + 2
            )

        for (
        i in melPoints.indices
        ) {

            melPoints[i] =
                melMin +
                        (
                                melMax -
                                        melMin
                                ) *
                        i /
                        (nMels + 1)
        }

        val hzPoints =
            DoubleArray(
                nMels + 2
            )

        for (
        i in hzPoints.indices
        ) {

            hzPoints[i] =
                melToHz(
                    melPoints[i]
                )
        }

        val bins =
            IntArray(
                nMels + 2
            )

        for (
        i in bins.indices
        ) {

            bins[i] =
                floor(
                    (
                            nFft + 1
                            ) *
                            hzPoints[i] /
                            sampleRate
                ).toInt()
        }

        for (
        m in 0 until nMels
        ) {

            val left =
                bins[m]

            val center =
                bins[m + 1]

            val right =
                bins[m + 2]

            for (
            k in left until center
            ) {

                if (
                    center != left
                ) {

                    filterBank[m][k] =
                        (
                                k - left
                                ).toFloat() /
                                (
                                        center -
                                                left
                                        ).toFloat()
                }
            }

            for (
            k in center until right
            ) {

                if (
                    right != center
                ) {

                    filterBank[m][k] =
                        (
                                right - k
                                ).toFloat() /
                                (
                                        right -
                                                center
                                        ).toFloat()
                }
            }

            val enorm =
                2.0 /
                        (
                                hzPoints[m + 2] -
                                        hzPoints[m]
                                )

            for (
            k in left until right
            ) {

                filterBank[m][k] =
                    (
                            filterBank[m][k] *
                                    enorm
                            ).toFloat()
            }
        }

        return filterBank
    }

    private fun hzToMel(
        hz: Double
    ): Double {

        return 2595.0 *
                log10(
                    1.0 +
                            hz / 700.0
                )
    }

    private fun melToHz(
        mel: Double
    ): Double {

        return 700.0 *
                (
                        exp(
                            mel /
                                    2595.0 *
                                    kotlin.math.ln(10.0)
                        ) - 1.0
                        )
    }

    // ============================================================
    // FFT
    // ============================================================

    private fun fft(
        real: FloatArray,
        imag: FloatArray
    ) {

        val n =
            real.size

        var j = 0

        for (
        i in 1 until n
        ) {

            var bit =
                n shr 1

            while (
                j and bit != 0
            ) {

                j =
                    j xor bit

                bit =
                    bit shr 1
            }

            j =
                j xor bit

            if (
                i < j
            ) {

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

        while (
            length <= n
        ) {

            val angle =
                -2.0 *
                        PI /
                        length

            val wLenReal =
                cos(angle)

            val wLenImag =
                sin(angle)

            var i = 0

            while (
                i < n
            ) {

                var wReal =
                    1.0

                var wImag =
                    0.0

                for (
                j2 in 0 until
                        length / 2
                ) {

                    val uReal =
                        real[
                            i + j2
                        ]

                    val uImag =
                        imag[
                            i + j2
                        ]

                    val vReal =
                        (
                                real[
                                    i +
                                            j2 +
                                            length / 2
                                ] *
                                        wReal -
                                        imag[
                                            i +
                                                    j2 +
                                                    length / 2
                                        ] *
                                        wImag
                                )

                    val vImag =
                        (
                                real[
                                    i +
                                            j2 +
                                            length / 2
                                ] *
                                        wImag +
                                        imag[
                                            i +
                                                    j2 +
                                                    length / 2
                                        ] *
                                        wReal
                                )

                    real[
                        i + j2
                    ] =
                        (
                                uReal +
                                        vReal
                                ).toFloat()

                    imag[
                        i + j2
                    ] =
                        (
                                uImag +
                                        vImag
                                ).toFloat()

                    real[
                        i +
                                j2 +
                                length / 2
                    ] =
                        (
                                uReal -
                                        vReal
                                ).toFloat()

                    imag[
                        i +
                                j2 +
                                length / 2
                    ] =
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

    // ============================================================
    // CLEANUP
    // ============================================================

    override fun onDestroy() {

        stopAssistant()

        ortSession?.close()

        ortSession = null

        speechRecognizer?.destroy()

        speechRecognizer = null

        super.onDestroy()
    }
}

// ================================================================
// UI
// ================================================================

@Composable
fun KabaliScreen(
    onStartListening: () -> Unit,
    onVoiceCommand: () -> Unit,
    onStop: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxSize(),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Kabali Assistant"
        )

        Button(
            onClick = onStartListening
        ) {

            Text(
                text = "Start Kabali Assistant"
            )
        }

        Button(
            onClick = onVoiceCommand
        ) {

            Text(
                text = "Test Voice Command"
            )
        }

        Button(
            onClick = onStop
        ) {

            Text(
                text = "Stop"
            )
        }
    }
}
