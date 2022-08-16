package com.deeplyinc.listen.samples.basic.simple

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
import com.deeplyinc.listen.sdk.Listen
import com.deeplyinc.listen.sdk.audio.classifiers.datastructures.ClassifierOutput
import com.deeplyinc.listen.sdk.exceptions.ListenAuthException
import com.deeplyinc.recorder.DeeplyRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleDeeplyRecorderActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SimpleDeeplyRecorder"
    }

    private lateinit var binding: ActivityBasicBinding

    private val listen = Listen(this)
    private lateinit var recorder: DeeplyRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Prevent screen off
        binding = DataBindingUtil.setContentView(this, R.layout.activity_basic)
        binding.lifecycleOwner = this

        initialize()
        configureLayout()
        requestRecordingPermission()
    }

    private fun initialize() {
        binding.start.isEnabled = false
        // Note that the init() takes time and blocks the thread during the initialization
        // process because it contains networking and file operations.
        // We recommend to call init() in the other thread like the following code.
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                listen.init("SDK KEY", "DPL ASSET PATH")

                recorder = DeeplyRecorder(
                    sampleRate = listen.getAudioParams().sampleRate,
                    bufferSize = listen.getAudioParams().inputSize
                )

                withContext(Dispatchers.Main) {
                    binding.start.isEnabled = true
                }
            } catch (e: ListenAuthException) {
                e.printStackTrace()
            }
        }
    }

    private fun configureLayout() {
        binding.start.setOnClickListener {
            if (recorder.isRecording()) {
                recorder.stop()
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
                recorder.start().collect {
                    runInference(it)
                }
            }
        }
    }

    private suspend fun runInference(audioSamples: ShortArray) {
        // run inference
        val result = listen.inference(audioSamples)
        withContext(Dispatchers.Main) {
            handleResult(result)
        }
    }

    private fun handleResult(result: ClassifierOutput) {
        // print result
        Log.d(TAG, "Inference result: ${result.event} ${result.confidence}")
        Log.d(TAG, "All results: ${result.rawResults}")

        // update UI
        binding.event.text = result.event
        binding.confidence.text = String.format("%.2f%%", result.confidence * 100.0)
    }
}