package com.nimrodtechs.rsock.publisher;

import com.nimrodtechs.rsock.common.SubscriptionRequest;
import com.nimrodtechs.rsock.serialization.KryoDecoder;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PublisherRSocket implements RSocket {

    final PublisherSocketImpl publisherSocket;

    public PublisherRSocket(PublisherSocketImpl publisherSocket) {
        this.publisherSocket = publisherSocket;
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        byte[] bytes = new byte[payload.data().readableBytes()];
        payload.data().readBytes(bytes);
        SubscriptionRequest subscriptionRequest = KryoDecoder.deserialize(bytes,SubscriptionRequest.class);
        publisherSocket.removeDirectProcessor(subscriptionRequest);
        return Mono.empty();
    }


    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        // Implementation here
        return Mono.empty();
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        byte[] bytes = new byte[payload.data().readableBytes()];
        payload.data().readBytes(bytes);
        SubscriptionRequest subscriptionRequest = KryoDecoder.deserialize(bytes,SubscriptionRequest.class);
        //DirectProcessor<Payload> processor = publisherSocket.addDirectProcessor(subscriptionRequest);
        Flux flux = publisherSocket.addDirectProcessor(subscriptionRequest);
        return flux;
    }

}
