# FAQ

## Too many one type of event are detected

Passing invalid audio sample data as input can cause too many events of one type to be detected.
For instance, if all input audio samples filled by zeros are passed as a input argument, they are processed normally by Listen because all zero audio samples are valid form of input argument. 
In most case, however, it is invalid input value due to implementation mistakes and errors, so Listen prints an warning message: 

```text
All input values are zero. The inference task will continue, but it is usually the result of bad recording.
```

The most common reason of the all zeros input is permission, recording from background, and microphone usage priority.


