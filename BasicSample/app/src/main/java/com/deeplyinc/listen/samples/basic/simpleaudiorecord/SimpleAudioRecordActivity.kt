package com.deeplyinc.listen.samples.basic.simpleaudiorecord

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
import com.deeplyinc.listen.samples.basic.databinding.ActivitySimpleAudioRecordBinding
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

    private lateinit var binding: ActivitySimpleAudioRecordBinding

    private val listen = Listen(this)
    private lateinit var audioRecord: AudioRecord
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Prevent screen off
        binding = DataBindingUtil.setContentView(this, R.layout.activity_simple_audio_record)
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
            if (isRecording) {
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
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val bufferSize = listen.getAudioParams().inputSize
        val buffer = ShortArray(bufferSize)
        val sampleRate = listen.getAudioParams().sampleRate
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        audioRecord.startRecording()
        isRecording = true
        lifecycleScope.launch(Dispatchers.Default) {
            while (isRecording) {
                // called every 0.1 second, buffer contains 1,000 samples
                audioRecord.read(buffer, 0, buffer.size)
                val result = listen.inference(buffer)
                withContext(Dispatchers.Main) {
                    handleResult(result)
                }
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord.stop()
        audioRecord.release()
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