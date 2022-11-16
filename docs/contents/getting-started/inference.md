# Inference

Listen provides three types of inference methods: inference, async inference, and batch.
We assume that we have already finished the initialization and audio recording process.

```kotlin
val listen = Listen(context)
listen.load("SDK_KEY", "MODEL_ASSETS_PATH")

val audioSamples = getAudioSamplesForMic() // get audio samples from mic using AudioRecord
```



## Simple Inference

The simplest and most straightforward method is synchronous inference.

```kotlin
// audioSamples must have fixed (pre-defined) sample rate and must be larger than or equal to the minimum input size, 
// which are available from listen.getAudioParams().sampleRate and listen.getAudioParams().minInputSize, respectively.
val results = listen.inference(audioSamples) 
```

Please note that this simple inference method `inference()` runs multiple inferences with variable-length audio samples which is at least `minInpustSize`.
You can get the minimum audio sample input size via `getAudioParams().minInputSize` property.



## Asynchronous Inference

If we want to detect a specific sound event in real-time, an asynchronous manner is better for sound event detection, especially because we cannot estimate when the sound event occurs.
Asynchronous detection is performed in the following manner:

1. Register a listener using `setAsyncInferenceListener()`
2. Pass audio samples recorded in real-time to `inferenceAsync(audioSamples)`
3. Listener or Kotlin coroutine will be invoked when the registered sound event is detected

<!--
### Register sound events

```kotlin
listen.registerEvent("cough")
// if you want to use custom threshold, specify the threshold value like below:
// listen.registerEvent("cough", threshold = 0.90)
```

The available sound events are listed in the documentation as well as the `getEventTypes()` method.

```kotlin
val eventTypes: List<String> = listen.getEventTypes()
```



### Pass recording audio samples

Transfer audio sample data collected in real-time to the `inferenceAsync(audioSamples)` method.



### Detect sound events

Listen provides two asynchronous detection methods: the listener-based method and Kotlin Coroutine-based method.

```kotlin

```

## Batch Inference

If you don't need real-time sound event analysis, or if you need to analyze large amounts of sound data at once, it's better to use batch analysis that handles more data at once than the default analysis method.
-->