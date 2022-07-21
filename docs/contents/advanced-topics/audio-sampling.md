# Audio Sampling

Audio sampling

## Basic Concept

The microphone collects sounds in analog signals in very short cycles. 
For example, the microphone can collect sound 1,000 times per second (every 0.001 second), or 10,000 times per second (every 0.0001 second). 
The process of collecting sound from the microphone is called *audio sampling* and the individual sound values are called *audio samples*. 
The number of times the sound is collected per second is called *sampling rate*.

For example, if you collect 1,000 audio samples per second, the sampling rate is 1,000 Hz or 1 KHz.
In addition, how many bits a single audio sample has is called *sample depth* or *bit depth*.
Android devices typically represent and store one audio sample in 16 bits (= 2 bytes). 
In other words, if you record for one second at a 1,000 Hz sampling rate with a 16 bit sample depth on one microphone, the capacity of this recorded sound data can be calculated as follows:

```
1 mic * 16 bit * 1 sec * 1,000 samples/sec
= 1 mic * 2 byte * 1 sec * 1,000 samples/sec
= 2,000 bytes = 2 KB
```

Since the sampling rate is how many audio samples there are in 1 second, if the other conditions are the same, the higher the sampling rate, the better the sound quality, and the higher the sampling rate, even if you record the sound for the same time, the higher the capacity. 
Typically, 8 KHz sampling rate is widely used for landline phones, and 44.1 KHz or 48 KHz sampling rate is widely used for music files such as WAV and MP3 files. 
This means that landline phones have a second of sound with 8,000 audio samples, and music files have a second of sound with 44,100 or 48,000 audio samples. 
Considering the difference in sound quality between landline phones and audio files, you can see the effect of this difference in sampling rates on sound quality. 

Knowing how to collect sound from a microphone like this is a great help in implementing sound handling capabilities.
Below, we will explain how this concept can be leveraged to implement features more efficiently.



## Recording and Minimum Buffer Size

On Android, recording works by using a buffer to read a certain number of audio samples collected through the microphone.

For example, theoretically, if you record at the same 10,000 Hz sampling rate, you can read one audio sample at a time, or you can read 1,000 audio samples in batches.
If we record at a 10,000 Hz sampling rate and read 1,000 audio samples at a time, we can process 1,000 new audio samples every 0.1 second.
If you implement this, it is as follows:

```kotlin
// DeeplyRecorder


// FIXME: this is impossible, just an example for explaining the concept
```

```kotlin
// AudioRecord


// FIXME: this is impossible, just an example for explaining the concept
```

This will cause the `runSomething()` function above to be invoked every time 1,000 new audio samples are gathered in the buffer, i.e. every 0.1 second.

Then wouldn't it always be best to bring a minimal audio sample?
If you run a task to update the UI in the `runSomething()` function, as shown in the example above, using the 1000 buffer size for the 10,000 Hz sampling rate can cause the UI to change every 0.1 second. 
But using a buffer size of 10 can cause the UI to change every 0.001 seconds. 
If you set the sampling rate to 1,000,000 Hz and set the buffer size to 1, you can create a function that responds really fast to sounds!

But in reality, that's impossible.
This is because the characteristics of the microphone determine the sampling rate that can be set and the minimum number of samples that can be imported at a time, i.e. the minimum buffer size. 

Therefore, the buffer size must be the smallest possible size to make it respond as fast as possible at a particular sampling rate. 
The `DeeplyRecorder` selects the minimum buffer size as the default value, so there is no need to set it up separately. 
When creating an object, `AudioRecord` must first determine the minimum buffer size through the ` ` method and then set this value through the `AudioRecord` constructor.

```kotlin
```

```kotlin
```



## Handle the Time with Sample Rate

If you have to cut the sound once every 10 seconds and save it as a `.wav` file, what should I do?
The most common method is to use the `postDeplayed()` method using `Hanlder` when functions need to be invoked at certain time intervals on Android.
But this is not a good way when you handle audio. 

<!-- 

If you want to find out more about why it's not a good method, please click 'View Details' below. 

1. Method to recreate an AudioRecord object every 10 seconds
2. Method to use one AudioRecord object but alternate between the startRecording() and stopRecording() functions
3. Method to use one AudioRecord object, accumulate audio samples in the buffer and get all the data stored at a fixed time interval

Typically, there are several reasons as follows:
- Some audio sample data is lost for a short period of time between the time it takes to create the AudioRecord object and the time between the startRecording() function and the time the `stopRecording()` function runs.
- Recalling the AudioRecord `startRecording()` and `stopRecording()` functions quickly often fails. 
- To prevent infinite accumulation of audio samples in the buffer and memory errors, it should be managed by methods such as emptying the memory continuously. 

-->

So what should I do?
You can adjust the size of the buffer. 
If we set the size of the buffer to `10 * sampling ratio` as shown below, the audio sample we get from the buffer will be exactly 10 seconds long audio.

```kotlin
val sampleRate = 16000
val recorder = DeeplyRecorder(bufferSize = 10 * 16000)
recorder.start().collect { audioSamples ->
    // audioSamples have 10 second length of audio samples
    buildWavFile(audioSamples)
}
```

Remembering that the number of audio samples means time can be of great help when implementing time-related audio features. 



## Applying to Listen

The Listen Sound Event AI analysis model was also created to match a specific sampling rate value.
Therefore, for Listen Sound Event AI to be able to properly analyze, the recording function must also be implemented according to the sampling rate of the pre-set AI model. 
The Listen SDK provides sampling rates for AI models using the `getAudioParams()` method.
If the recording function is implemented using `DeeplyRecorder`, it can be used as follows:

```
val listen = Listen(this)
listen.init("SDK_KEY", "DPL FILE ASSETS PATH")

val sampleRate = listen.getAudioParams().sampleRate
val recorder = DeeplyRecorder(sampleRate = sampleRate)
```

<!-- 
Caution!
The sampling rate value may vary depending on the file `.dpl`. 
If you use the recording function for both Listen and other purposes at the same time, you should write the code so that there is no problem even if the sampling rate changes during recording.
-->

