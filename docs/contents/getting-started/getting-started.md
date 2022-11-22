# Getting Started

## Dependency

Add the following line in your module level `build.gradle` file:

```groovy
dependencies {
    implementation "com.deeplyinc.listen.sdk:listen:VERSION"
}
```



## Permission

Listen also requires audio recording permission and Internet permission.
The permissions are needed for the following purpose:

- `RECORD_AUDIO` for recording audio
- `INTERNET` for authentication and authorization of Listen SDK. Recording audio is never sent to the server.

Add the following lines to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

`RECORD_AUDIO` permission is strongly related to privacy issues, so it is considered a runtime permission or [dangerous permission](https://developer.android.com/guide/topics/permissions/overview#runtime) in the Android framework. 
We need additional permission approval for audio recording from the user.
More detailed information is available in [Audio Recording](audio-recording).



## Initialization

To use Listen, you must first create a Listen instance and initialize it using the SDK key and `.dpl` file.


### The Simplest Method

The simplest form of initialization code is as follows.

```kotlin
class MainActivity : AppCompatActivity() {
    
    // Creat a Listen instance
    private val listen = Listen(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Listen with SDK key and .dpl file
        listen.load("SDK KEY", "DPL FILE ASSETS PATH")
    }
}
```

At this time, put the `.dpl` file in the `assets` folder and pass the file name to the `init()` function.
For example, if you put the `listen.dpl` file in the `app/src/main/assets/model.dpl` location, your code should be as follows.

```kotlin
listen.load("SDK KEY", "listen.dpl")
```

The above code works.
However, in order to use Listen more reliably, we recommend considering the following points.


### Thread Blocking Issue

The `load()` method internally performs various tasks such as SDK authentication and deep learning model loading. 
This process may take several seconds depending on the device's performance and network status.
Because initialization operations are synchronous rather than asynchronous, threads can be blocked while processing these operations.
Therefore, if initialization is performed on the main thread, [ANR (Application Not Responding)] (https://developer.android.com/topic/performance/vitals/anr) may occur due to thread blocking.

To solve this problem, we recommend runnig the `load()` function in a separate thread.
Below is an example using Kotlin coroutines.

```kotlin
// Note that the load() takes time and blocks the thread during the initialization
// process because it contains networking and file operations.
// We recommend calling load() in other thread like the following code.
lifecycleScope.launch(Dispatchers.Default) {
    listen.load("SDK KEY", "DPL FILE ASSETS PATH")
}
```


### Exception Handling

The `load()` function can raise an exception for a number of reasons.
Typically, there are cases where the `.dpl` file does not exist, if the connection to the authentication server fails due to an internet connection problem, if the period of use of the SDK key has expired, etc.
If these exceptions are not handled, the app will be crashed and be forced to close.
Therefore, for the stablility and reliability of the app, the exception handling for the situation in which an exception occurs should be done as follows.

```kotlin
try {
    listen.load("SDK KEY", "DPL FILE ASSETS PATH")
} catch (e: Exception) {
    // Handle exceptions
}
```


### A Recommended Way

This is a recommended example initialization code that reflects the considerations introduced above.

```kotlin
class MainActivity : AppCompatActivity() {
    
    // Create a Listen instance
    private val listen = Listen(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Note that the load() takes time and blocks the thread during the initialization
        // process because it contains networking and file operations.
        // We recommend calling load() in other thread like the following code.
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                // Initialize Listen with SDK key and .dpl file
                listen.load("SDK KEY", "DPL FILE ASSETS PATH")
            } catch (e: Exception) {
                // Handle authentication exception
            }
        }
    }
}
```
