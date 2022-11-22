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

        // Note that the load() takes time and blocks the thread during the initialization
        // process because it contains networking and file operations.
        // We recommend to call load() in the other thread like the following code.
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                listen.load("SDK KEY", "DPL ASSET PATH")

                recorder = DeeplyRecorder(
                    sampleRate = listen.getAudioParams().sampleRate,
                    bufferSize = listen.getAudioParams().minInputSize
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
                recorder?.start()?.collect {
                    val audioSamples = it.map { it.toDouble() }.toDoubleArray()
                    val results = listen.inference(audioSamples)
                    Log.d(TAG, "Results: $results")

                    handleResults(results)
                }
            }
        }
    }

    private fun stopRecording() {
        if (recorder?.isRecording() == true) {
            recorder?.stop()
        }
    }

    private suspend fun handleResults(results: List<ClassifierOutput>) {
        withContext(Dispatchers.Main) {
            for (result in results) {
                // update UI
                binding.event.text = result.event
                binding.confidence.text = String.format("%.2f%%", result.confidence * 100.0)
            }
        }
    }
}