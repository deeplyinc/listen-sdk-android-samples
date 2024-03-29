# Quick Start

In this page, we introduce the simplest way to use Listen.
In addition to the analysis capabilities, there are many other features that need to be implemented together, such as requesting microphone permissions and recordings. 
The Listen SDK comes with a variety of tools that make it easy to use all the features needed to analyze sound events. 
This article introduces the way to quickly implement Sound event analysis by using these tools.

Of course, even if you don't use the tools provided by the Listen SDK and implement them directly in the way provided by the Android framework, you can use the same Listen sound analysis functionality.
The direct implementation in the manner provided by the Android framework will be described in more detail in the following documentation.

We assume that you already have an SDK key and `.dpl` file that are provided after service registration. 



## Add Dependencies

Add the following line to the module-level `build.gradle` file:

```groovy
implementation "com.deeplyinc.listen.sdk:listen:VERSION"
```



## Add Permissions

`RECORD_AUDIO` and `INTERNET` permissions are required to use Listen.
Declare permission to file `AndroidManifest.xml` as follows:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```



## Listen Initialization

Initialize Listen using the SDK key and the `.dpl` file as shown below.
Put the `.dpl` file in the `assets` folder of the app and enter the file name. 

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var listen: Listen
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ... 

        // Initialize Listen SDK with SDK key and .dpl file
        // This example code is only for giving quick understanding of how we can use Listen SDK. 
        // If the .dpl file is large, it will take a long time to load .dpl file, 
        // so it is not recommended to use load() method on main thread in practical use. 
        listen = Listen(this)
        listen.load("SDK KEY", "DPL ASSET PATH")
    }
}

```



## Audio Recordings

To use Listen's sound analysis, you must implement a recording function.
This section describes how to implement it using the `DeeplyRecorder` that comes with the Listen SDK.
If you want to find out how to implement it directly using `AudioRecord` provided by Android, please visit [Audio Recording](audio-recording).
If the recording function is already implemented, you can skip this part. 

Before implementing the recording function, you must first implement the function that asks the user for permission to record as follows:

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- RECORD_AUDIO permission -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    ...

</manifest>
```

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var listen: Listen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        listen = Listen(this)
        listen.load("SDK KEY", "DPL ASSET PATH")

        // Request recording permission
        requestRecordingPermission()
    }

    private fun requestRecordingPermission() {
        val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "Recording permission is granted")
            }
        }
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }
}
```

Now implement the recording function.
It is easy to implement using the `DeeplyRecorder` included in the Listen SDK. 

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var listen: Listen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        listen = Listen(this)
        listen.load("SDK KEY", "DPL ASSET PATH")

        requestRecordingPermission()
    }

    private fun requestRecordingPermission() {
        val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "Recording permission is granted")
                
                // start recording if the user grants the permission
                startRecording()
            }
        }
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        val sampleRate = listen.getAudioParams().sampleRate
        val minInputSize = listen.getAudioParams().minInputSize
        val recorder = DeeplyRecorder(
            sampleRate = sampleRate,
            bufferSize = minInputSize
        )
        recorder.start().collect { audioSamples ->
            // recording started!
        }
    }
}
```



## Sound Analysis

The recording has started, and all preparations are now complete!
To start analyzing Listen sound events, you can start recording and then forward the recorded audio sample data to Listen. 
We're going to talk about it in the simplest way, the basic analysis way.
Refer to the Sound Event Analysis documentation for a detailed description of the different analysis methods provided by Listen.

```kotlin
private fun startRecording() {
    val sampleRate = listen.getAudioParams().sampleRate
    val minInputSize = listen.getAudioParams().minInputSize
    val recorder = DeeplyRecorder(
        sampleRate = sampleRate,
        bufferSize = minInputSize
    )
    recorder.start().collect { audioSamples ->
        val results = listen.inference(audioSamples)
        // Inference results here!
        Log.d("Listen", results)
    }
}
```

Analysis completed!
The data recorded in real time is now continuously coming in through the `audioSamples` value, which can be passed to the `inference()` function to see the result through the `results` variable!

Now all we have to do is make your app even better. 

If you want to know more about how to use the Listen SDK, you can check it out in the following documents. 


