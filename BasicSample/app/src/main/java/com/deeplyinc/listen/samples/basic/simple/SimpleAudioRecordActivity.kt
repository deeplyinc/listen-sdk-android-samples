package com.deeplyinc.listen.samples.basic.simple

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
import com.deeplyinc.listen.sdk.Listen
import com.deeplyinc.listen.sdk.audio.classifiers.datastructures.ClassifierOutput
import com.deeplyinc.listen.sdk.exceptions.ListenAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleAudioRecordActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SimpleAudioRecordActivity"
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
        // Note that the load() takes time and blocks the thread during the initialization
        // process because it contains networking and file operations.
        // We recommend to call load() in the other thread like the following code.
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                listen.load("SDK KEY", "DPL ASSET PATH")

                withContext(Dispatchers.Main) {
                    binding.start.isEnabled = true
                }
            } catch (e: ListenAuthException) {
                e.printStackTrace()
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

        val bufferSize = listen.getAudioParams().minInputSize
        val buffer = ShortArray(bufferSize)
        val sampleRate = listen.getAudioParams().sampleRate
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.size)
        if (audioRecord?.state == AudioRecord.STATE_UNINITIALIZED) {
            Log.w(TAG, "Failed to initialize AudioRecord", )
            return
        }
        audioRecord?.startRecording()
        isRecording = true

        lifecycleScope.launch(Dispatchers.Default) {
            while (isRecording) {
                // called every 0.1 second, buffer contains 1,000 samples
                audioRecord?.read(buffer, 0, buffer.size)

                // Run inference. Please note that inference is time-consuming task, so running
                // inference in the main thread results in thread blocking issue.
                val results = listen.inference(buffer)
                Log.d(TAG, "Results: $results")

                handleResults(results)
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