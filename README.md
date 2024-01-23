# nimrod-ipc-rsock
Nimrod Inter-Process Communication Mk 2

This is a re-implementation of Nimrod IPC which was based on ZeroMQ to now be based on SpringBoot RSocket Reactive Streams.

This has lots of advantages. Mostly that it becomes a Java only implementation and therefore easier to deploy assuming Spring/SpringBoot framework already in use.
I was experiencing some message dropping under extreme load on the ZeroMQ version and this now seems to be addressed. 
I have retained pretty much all of the api calls' behaviour and functionality of the ZeroMQ based version but Pub/Sub publishing many-to-one is not an option BUT there is a simple and effective replacement. In the RemoteServerService implemetation I provide a 'fireAndForget' operation to use which preforms a 'send' on the Rsocket client and which is a non-blocking, async call from the caller perespective. This is the equalivalent of the 'many' publishers all publishing to the 'one' receiver. Meanwhile on the receiver side i.e. the Server, the @MessageMapping corresponding 'routed to' method that method returns void or Mono<Void>. 

More documentation to follow. 
