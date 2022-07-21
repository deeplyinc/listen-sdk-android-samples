# Getting Started

## Dependency

Add the following line in your module level `build.gradle` file:

```groovy
dependencies {
    implementation "com.deeplyinc.listen:listen:VERSION"
    implementation "com.deeplyinc.library.audioutils:audioutils:VERSION" // optional: audioutils
}
```

## Permission

Listen also requires audio recording permission and Internet permission.
The permissions are needed for the following purpose:

- `RECORD_AUDIO` for recording audio
- `INTERNET` for authentication and authorization of Listen SDK. Recording audio are never sent to server.

Add following lines to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

`RECORD_AUDIO` permission is strongly related to privacy issues, so it is considered as a runtime permission or [dangerous permission](https://developer.android.com/guide/topics/permissions/overview#runtime) in the Android framework. 
We need additional permission approval of audio recording from user.
The detail information is described in [Audio Recording](audio-recording).


## Initialization


```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ... 

        // Initialize Listen with SDK key and .dpl file
        val listen = Listen(this)
        listen.init("SDK KEY", "DPL FILE ASSETS PATH")
    }
}
```


```kotlin
listen.init("SDK KEY", "listen.dpl")
```
