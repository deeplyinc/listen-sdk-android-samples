# FAQ

FAQ



## Too many events are detected

When all audio samples are filled by zeros due to implementation mistakes and errors, it results in invalid inference result of Listen.
If all audio sample are filled by zeros, Listen prints an warning message: 

```text
All input values are zero. The inference task will continue, but it is usually the result of bad recording.
```

The most common reason is permission, 

One other case is when your application is in the background, not foreground.
Audio recording is regarded as dangerous permission. 
when your recording is implemented in background service.
When your application is 
