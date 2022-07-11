# Installation

hello

## Hello

hello, world! 

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
Add following lines to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```