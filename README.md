# nimrod-ipc-rsock
Nimrod Inter-Process Communication Mk 2

This is a re-implementation of Nimrod IPC which was based on ZeroMQ to now be based on SpringBoot RSocket Reactive Streams.

This has lots of advantages. Mostly that it becomes a Java only implementation and therefore easier to deploy assuming Spring/SpringBoot framework already in use.
I have retained pretty all of the api calls' behaviour and functionality of the ZeroMQ based version. I was experiencing some message dropping under extreme load on the ZeroMQ version and this now seems to be addressed. 

More documentation to follow. 
