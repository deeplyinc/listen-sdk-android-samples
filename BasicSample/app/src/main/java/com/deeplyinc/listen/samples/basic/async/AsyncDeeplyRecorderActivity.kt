package com.deeplyinc.listen.samples.basic.async

import android.Manifest
import android.content.pm.PackageManager
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
import com.deeplyinc.recorder.DeeplyRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsyncDeeplyRecorderActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AsyncDeeplyRecorderActivity"
    }

    private val listen = Listen(this)

    private var recorder: DeeplyRecorder? = null

    private lateinit var binding: ActivityBasicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Prevent screen off
        binding = DataBindingUtil.setContentView(this, R.layout.activity_basic)
        binding.lifecycleOwner = this

        initialize()
        configureLayout()

        requestRecordingPermission()
    }

    override fun onStop() {
        super.onStop()

        stopRecording()
    }

    private fun initialize() {
        binding.start.isEnabled = false

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

                recorder = DeeplyRecorder(
                    sampleRate = listen.getAudioParams().sampleRate,
                    // Note that buffer size is not specified. In asynchronous inference, we can freely
                    // append any size of audio samples to the Listen. Listen will temporarily
                    // store the audio samples and analyze them when the audio samples reach enough
                    // size.
                )

                withContext(Dispatchers.Main) {
                    binding.start.isEnabled = true
                }
            } catch (e: ListenAuthException) {
                e.printStackTrace()
            }

            // Receive the inference result using Kotlin flow.
            listen.resultFlow().collect {
                Log.d(TAG, "Results handled by Kotlin Flow: $it")
                handleResult(it)
            }
        }
    }

    private fun configureLayout() {
        binding.start.setOnClickListener {
            if (recorder?.isRecording() == true) {
                stopRecording()
                binding.start.text = "Start"
            } else {
                startRecording()
                binding.start.text = "Stop"
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Recording permission is not granted ")
            return
        } else {
            lifecycleScope.launch {
                recorder?.start()?.collect { audioSamples ->
                    // Run async inference
                    listen.inferenceAsync(audioSamples)
                }
            }
        }
    }

    private fun stopRecording() {
        if (recorder?.isRecording() == true) {
            recorder?.stop()
        }
    }

    private suspend fun handleResult(result: ClassifierOutput) {
        withContext(Dispatchers.Main) {
            // update UI
            binding.event.text = result.event
            binding.confidence.text = String.format("%.2f%%", result.confidence * 100.0)
        }
    }
}