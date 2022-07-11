# FAQ

FAQ



## Too many one type of event are detected

If too many events of one type are detected, Listen may be passing invalid audio sample data as input.
For instance, if all input audio samples filled by zeros are passed as a input argument, they are processed normally by Listen because all zero audio samples are valid form of input argument. 
In most case, however, it is invalid input value due to implementation mistakes and errors, so Listen prints an warning message: 

```text
All input values are zero. The inference task will continue, but it is usually the result of bad recording.
```

The most common reason of the all zeros input is permission, background issue, and microphone usage priority.

### Permission

### Foreground and Background

One other case is when your application is in the background, not foreground.
Audio recording is regarded as dangerous permission. 
when your recording is implemented in background service.
When your application is 

### Mic Usage Priority


