package com.deeplyinc.listen.samples.basic.simpledeeplyrecorder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.deeplyinc.listen.samples.basic.R
import com.deeplyinc.listen.samples.basic.databinding.ActivitySimpleDeeplyRecorderBinding
import com.deeplyinc.listen.sdk.Listen
import com.deeplyinc.recorder.DeeplyRecorder

class SimpleDeeplyRecorderActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimpleDeeplyRecorderBinding

    private lateinit var listen: Listen
    private lateinit var recorder: DeeplyRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_simple_deeply_recorder)

        listen = Listen(this)
        recorder = DeeplyRecorder()
    }
}