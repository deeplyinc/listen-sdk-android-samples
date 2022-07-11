# Inference

Listen provides three types of inference methods: inference, async inference, and batch.
We assume that we already finish initialization and audio recording process.

```kotlin
val listen = Listen(context)
listen.initialze("SDK_KEY", "MODEL_ASSETS_PATH")

val audioSamples = getAudioSamplesForMic() // get audio samples from mic using AudioRecord
```


## Simple Inference

The simplest and straight-forward method is synchronous inference.

```kotlin
// audioSamples must have fixed (pre-defined) sample rate and input size, 
// which is available from listen.getAudioParams().sampleRate and 
// listen.getAudioParams().inputSize, respectively.
val result = listen.analyze(audioSamples) 
```

Please note that this simple inference method runs a single inference with a fixed length audio samples, so the size of input audio samples must match the required size.
You can get the required audio sample input size via `getAudioParams().inputSize` property.


## Asynchronous Inference

If we want to detect a specific sound event in real-time, asynchronos manner is better for sound event detection especially because we cannot estimate when the sound event occurs.
Asynchronous detection performed as following manner:

1. Register sound events we want to detect
2. Pass audio samples recorded in real-time to `inferenceAsync(audioSamples)` method
3. Listener or Kotlin coroutine will be invoked when the registered sound event is detected

### Register sound events

Sound event registration is 

```kotlin
listen.registerEvent("cough")
// if you want to use custom threshold, specify the threshold value like below:
// listen.registerEvent("cough", threshold = 0.90)
```

The available sound events are listed in the documentation as well as `getEventTypes()` method.

### Pass recording audio samples

hello, world! 

### Detect sound events

Listen provides two asynchronous detection methods: listener-based method and Kotlin Coroutine-based method.

```kotlin
aaaaa
```

## Batch Inference

hello, world! 
