package com.deeplyinc.listen.samples.basic.async

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deeplyinc.listen.samples.basic.R
import com.deeplyinc.listen.samples.basic.databinding.ActivityBasicBinding
import com.deeplyinc.listen.sdk.AudioEventClassificationListener
import com.deeplyinc.listen.sdk.Listen
import com.deeplyinc.listen.sdk.audio.classifiers.datastructures.ClassifierOutput
import com.deeplyinc.listen.sdk.exceptions.ListenAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsyncAudioRecordActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AsyncAudioRecordActivity"
    }

    private val listen = Listen(this)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private lateinit var binding: ActivityBasicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Prevent screen off
        binding = DataBindingUtil.setContentView(this, R.layout.activity_basic)
        binding.lifecycleOwner = this

        configureLayout()
        initialize()

        requestRecordingPermission()
    }

    override fun onStop() {
        super.onStop()

        stopRecording()
    }

    private fun configureLayout() {
        binding.start.isEnabled = false
        binding.start.setOnClickListener {
            if (isRecording) {
                stopRecording()
                binding.start.text = "Start"
            } else {
                startRecording()
                binding.start.text = "Stop"
            }
        }
    }

    private fun initialize() {
        // Note that the init() takes time and blocks the thread during the initialization
        // process because it contains networking and file operations.
        // We recommend to call init() in the other thread like the following code.
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                listen.load("SDK KEY", "DPL ASSET PATH")

                // You need to add a listener, because inferenceAsync() runs in asynchronous
                // manner. We cannot know when Listen will give the inference result to us, so
                // register the listener to handle the inference result.
                // You can also use Kotlin coroutine to handle the inference result by using
                // resultFlow(). In that case you don't need to register the listener.
                listen.setAsyncInferenceListener(object : AudioEventClassificationListener {
                    override fun onDetected(result: ClassifierOutput) {
                        Log.d(TAG, "Results handled by listener: $result")
                    }
                })

                withContext(Dispatchers.Main) {
                    binding.start.isEnabled = true
                }
            } catch (e: ListenAuthException) {
                e.printStackTrace()
            }

            // Receive the inference result using Kotlin flow.
            listen.resultFlow().collect {
                Log.d(TAG, "Results handled by Kotlin Flow: $it")
                handleResults(it)
            }
        }
    }

    private fun requestRecordingPermission() {
        val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "Recording permission is granted")
            }
        }
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val channel = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBufferSize = AudioRecord.getMinBufferSize(
            listen.getAudioParams().sampleRate,channel,
            audioFormat
        )
        // Note that buffer size is set to 2 * minBufferSize, because in asynchronous inference,
        // we can freely append any size of audio samples to the Listen. Listen will temporarily
        // store the audio samples and analyze them when the audio samples reach enough size.
        val buffer = ShortArray(2 * minBufferSize)
        val sampleRate = listen.getAudioParams().sampleRate
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioFormat, buffer.size)
        if (audioRecord?.state == AudioRecord.STATE_UNINITIALIZED) {
            Log.w(TAG, "Failed to initialize AudioRecord", )
            return
        }
        audioRecord?.startRecording()
        isRecording = true
        lifecycleScope.launch(Dispatchers.Default) {
            while (isRecording) {
                // Read the audio samples into buffer
                audioRecord?.read(buffer, 0, buffer.size)

                // Run async inference
                listen.inferenceAsync(buffer)
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            isRecording = false
        }
        if (audioRecord != null && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord?.stop()
            audioRecord?.release()
        }
    }

    private suspend fun handleResults(result: ClassifierOutput) {
        withContext(Dispatchers.Main) {
            // update UI
            binding.event.text = result.event
            binding.confidence.text = String.format("%.2f%%", result.confidence * 100.0)
        }
    }
}