package com.deeplyinc.listen.samples.basic.simpledeeplyrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deeplyinc.listen.samples.basic.R
import com.deeplyinc.listen.samples.basic.databinding.ActivitySimpleDeeplyRecorderBinding
import com.deeplyinc.listen.sdk.Listen
import com.deeplyinc.recorder.DeeplyRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleDeeplyRecorderActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SimpleDeeplyRecorder"
    }

    private lateinit var binding: ActivitySimpleDeeplyRecorderBinding

    private lateinit var listen: Listen
    private lateinit var recorder: DeeplyRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_simple_deeply_recorder)
        binding.lifecycleOwner = this

        initialize()

        configureLayout()
    }

    private fun initialize() {
        listen = Listen(this)
        listen.init("SDK KEY HERE", ".dpl FILE ASSETS PATH HERE")

        recorder = DeeplyRecorder(
            sampleRate = listen.getAudioParams().sampleRate,
            bufferSize = listen.getAudioParams().inputSize
        )
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

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            DeeplyRecorder.requestRecordingPermission(this) {
                Log.d(TAG, "Recording permission granted ")
                lifecycleScope.launch {
                    recorder.start().collect {
                        runInference(it)
                    }
                }
            }
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

        // print result
        Log.d(TAG, "Inference result: ${result.event} ${result.confidence}")
        Log.d(TAG, "All results: $result")

        // update UI
        withContext(Dispatchers.Main) {
            binding.event.text = result.event
            binding.confidence.text = String.format("%.2f", result.confidence * 100.0)
        }
    }
}