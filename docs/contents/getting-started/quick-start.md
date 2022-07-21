# Quick Start

In this page, we introduce the simplest way to use Listen.
In addition to the analysis capabilities, there are many other features that need to be implemented together, such as requesting microphone permissions and recordings. 
The Listen SDK comes with a variety of tools that make it easy to use all the features needed to analyze sound events. 
This article introduces the way to quickly implement Sound event analysis by using these tools.

Of course, even if you don't use the tools provided by the Listen SDK and implement them directly in the way provided by the Android framework, you can use the same Listen sound analysis function.
The direct implementation in the manner provided by the Android framework will be described in more detail in the following documentation.

We assume that you already have an SDK key and `.dpl` file that is provided after service registration. 



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

        // Initialize Listen with SDK key and .dpl file
        listen = Listen(this)
        listen.init("SDK KEY", "DPL FILE ASSETS PATH")
    }
}

```


## Audio recordings

To use Listen's sound analysis, you must implement a recording function.
This section describes how to implement it using the `DeeplyRecorder` that comes with the Listen SDK.
If you want to find out how to implement it directly using `AudioRecord` provided by Android, please visit [Audio Recording](audio-recording).
If the recording function is already implemented, you can skip this part. 

Before implementing the recording function, you must first implement the function that asks the user for permission to record as follows:

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var listen: Listen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...

        listen = Listen(this)
        listen.init("SDK KEY", "DPL FILE ASSETS PATH")

        // request audio recording permission
        DeeplyRecorder.requestAudioPermission() { isGranted ->
            if (isGranted) {
                // RECORD_AUDIO permission is granted
            }
        }
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
        listen.init("SDK KEY", "DPL FILE ASSETS PATH")

        DeeplyRecorder.requestAudioPermission() { isGranted ->
            if (isGranted) {
                // start recording if the user grants the permission
                startRecording()
            }
        }
    }

    private fun startRecording() {
        val sampleRate = listen.getAudioParams().sampleRate
        val recorder = DeeplyRecorder(
            sampleRate = sampleRate,
            bufferSize = sampleRate
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
recorder.start().collect { audioSamples ->
    val result = listen.inference(audioSamples)
    Log.d("Listen", result)
}
```

Analysis completed!
The data recorded in real time is now continuously coming in through the `audioSamples` value, which can be passed to the `inference()` function to see the result through the `result` variable!

Now all we have to do is make your app even better. 

If you want to know more about how to use the Listen SDK, you can check it out in the following documents. 


