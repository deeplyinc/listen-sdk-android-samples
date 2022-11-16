# Audio Recording

To use the Listen sound event detection feature, we need to record audio from an Android device. 
Listen SDK provides `DeeplyRecorder` for the easy implementation of audio recording, which is a Kotlin Coroutine wrapper of AudioRecord. 
It is also possible to use Listen via `AudioRecord` and we introduce both methods.


## Permission Request

Before implementing the recording function, you must first implement the function that asks the user for permission to record as follows:

```kotlin
class MainActivity : AppCompatActivity() {

    // for RECORD_AUDIO permission request
    private val requestRecordPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Audio recording permission is granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        val listen = Listen(this)
        listen.load("SDK KEY", "DPL FILE ASSETS PATH")

        // Request RECORD_AUDIO permission
        requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
    }
}
```


## DeeplyRecorder

`DeeplyRecorder` is a Deeply's open-source library, built into the Listen SDK, that helps you easily use `AudioRecord` using Kotlin Coroutine to implement recordings. 
Here's how to use this `DeeplyRecorder` to implement the recording function.
For more information on `DeeplyRecorder`, see [Official Document] (https://deeply-recorder-android.readthedocs.io) or [GitHub Link] (https://github.com/deeplyinc/deeply-recorder-android)).


### Recording

Using the Listen SDK, you can easily implement the recording function as follows: 

```kotlin
val recorder = DeeplyRecorder()
recorder.start().collect { audioSamples ->
    //
}
```

When implementing a recording feature, you should decide where to implement it, depending on the capabilities and usage of the app. 
Typically, it is common to implement it to work with one of these: Activity (View Model), Service, and ForegroundService.
More information on this is provided under [Background Recording](../advanced-topics/background-recording). 

You are now ready to use Listen's sound analysis. 
Proceed to the Sound Event Analysis document.



## AudioRecord

### Recording

You can use `AudioRecord` or `MediaRecorder` to implement the recording function on Android. 
For Listen integration, `AudioRecord` is more suitable for real-time processing of original audio data. 
For more information on `AudioRecord`, visit [Official Document](https://developer.android.com/reference/android/media/AudioRecord).

```kotlin
class MainActivity : AppCompatActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // start recording if permission is granted
            startRecording()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        val listen = Listen(this)
        listen.load("SDK KEY", "DPL FILE ASSETS PATH")

        requestPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        val sampleRate = listen.getAudioParams().sampleRate
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            sampleRate // bufferSizeInBytes
        )
    }
}
```

[Sample Apps](https://github.com/deeplyinc/listen-sdk-android-samples) for examples of actual implementations. 

The recording function implementation is complete. 
Now move on to the Sound Event Analysis document to use the Sound Analysis feature in Listen. 

