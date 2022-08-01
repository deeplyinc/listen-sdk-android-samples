package com.deeplyinc.listen.samples.basic.simpleaudiorecord

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.deeplyinc.listen.samples.basic.R
import com.deeplyinc.listen.samples.basic.databinding.ActivitySimpleAudioRecordBinding

class SimpleAudioRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimpleAudioRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_simple_audio_record)


    }
}