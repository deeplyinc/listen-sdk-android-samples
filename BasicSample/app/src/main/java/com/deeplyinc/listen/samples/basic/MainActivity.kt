package com.deeplyinc.listen.samples.basic

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.deeplyinc.listen.samples.basic.databinding.ActivityMainBinding
import com.deeplyinc.listen.samples.basic.simpleaudiorecord.SimpleAudioRecordActivity
import com.deeplyinc.listen.samples.basic.simpledeeplyrecorder.SimpleDeeplyRecorderActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnSimpleAudioRecord.setOnClickListener { startNewActivity(it) }
        binding.btnSimpleDeeplyRecorder.setOnClickListener { startNewActivity(it) }
    }

    private fun startNewActivity(v: View) {
        val targetActivity: Class<*> = when (v.id) {
            R.id.btn_simple_audio_record -> SimpleAudioRecordActivity::class.java
            R.id.btn_simple_deeply_recorder -> SimpleDeeplyRecorderActivity::class.java
            else -> null
        } ?: return
        startActivity(Intent(this, targetActivity))
    }
}