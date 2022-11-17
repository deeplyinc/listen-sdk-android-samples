package com.deeplyinc.listen.samples.basic.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.deeplyinc.listen.samples.basic.R
import com.deeplyinc.listen.sdk.Listen

class RecordingForegroundService : Service() {
    companion object {
        const val TAG = "RecordingForegroundService"
        private const val NOTIFICATION_ID = 0x1234
        private const val NOTIFICATION_CHANNEL_ID = "notification_channel_recording"
        private const val NOTIFICATION_CHANNEL_NAME = "Listen"
    }

    private val listen = Listen(this)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        startRecording()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopRecording()

        return super.onDestroy()
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "RECORD_AUDIO permission is not granted", )
            return
        }

        val runnable = Runnable {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

            listen.load("SDK KEY", "DPL ASSET PATH")

            val bufferSize = listen.getAudioParams().minInputSize
            val buffer = ShortArray(bufferSize)
            val sampleRate = listen.getAudioParams().sampleRate
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.size)
            if (audioRecord?.state == AudioRecord.STATE_UNINITIALIZED) {
                Log.w(TAG, "Failed to initialize AudioRecord", )
                return@Runnable
            }
            audioRecord?.startRecording()
            isRecording = true

            while (isRecording) {
                audioRecord?.read(buffer, 0, buffer.size)

                // Run inference. Please note that inference is time-consuming task, so running
                // inference in the main thread results in thread blocking issue.
                val results = listen.inference(buffer)
                Log.d(TAG, "Results: $results")
            }
        }

        val t = Thread(runnable);
        t.start()
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

    private fun createNotificationChannel(channelId: String, channelName: String) {
        val channel = NotificationChannel(
            channelId, channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun startForeground() {
        createNotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)

        val pendingIntent: PendingIntent =
            Intent(this, ForegroundServiceActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Listen")
            .setContentText("Listen is recording...")
            .setSmallIcon(R.drawable.ic_baseline_mic_24)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): RecordingForegroundService = this@RecordingForegroundService
    }
}