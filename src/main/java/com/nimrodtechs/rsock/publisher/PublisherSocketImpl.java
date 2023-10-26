package com.nimrodtechs.rsock.publisher;

import com.nimrodtechs.rsock.common.PublisherPayload;
import com.nimrodtechs.rsock.common.SubscriptionDirective;
import com.nimrodtechs.rsock.common.SubscriptionListener;
import com.nimrodtechs.rsock.common.SubscriptionRequest;
import com.nimrodtechs.rsock.serialization.KryoEncoder;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublisherSocketImpl implements SocketAcceptor {

    @Value("${nimrod.rsock.publisher.port:60281}")
    int publisherPort;

    class SubscriberFluxInfo {
        String subscriberName;
        String modifiedSubject;
        List<String> subscriberNames = new ArrayList<>();
        DirectProcessor<Payload> directProcessor;
        Sinks.Many<Payload> sink;

        public SubscriberFluxInfo(String subscriberName, DirectProcessor<Payload> directProcessor, String unmodifiedSubject) {
            this.subscriberName = subscriberName;
            this.directProcessor = directProcessor;
            if(unmodifiedSubject.endsWith("*")) {
                modifiedSubject = unmodifiedSubject.substring(0,unmodifiedSubject.lastIndexOf("*"));
            } else {
                modifiedSubject = unmodifiedSubject;
            }
        }
        public SubscriberFluxInfo(String subscriberName, Sinks.Many<Payload> sink, String unmodifiedSubject) {
            this.subscriberName = subscriberName;
            this.sink = sink;
            if(unmodifiedSubject.endsWith("*")) {
                modifiedSubject = unmodifiedSubject.substring(0,unmodifiedSubject.lastIndexOf("*"));
            } else {
                modifiedSubject = unmodifiedSubject;
            }
        }
    }


    private final Map<String, SubscriberFluxInfo> subjectProcessors = new ConcurrentHashMap<>();
    private final Map<String, SubscriberFluxInfo> wildcardProcessors = new ConcurrentHashMap<>();

    private final List<SubscriptionListener> subscriptionListeners = new ArrayList<>();

    @PostConstruct
    void init() {
        RSocketServer.create(this)
                .bind(TcpServerTransport.create(publisherPort)) // Specify your port here
                .subscribeOn(Schedulers.boundedElastic()) // Use a different thread pool
                .subscribe();

    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload, RSocket rSocket) {
        return Mono.just(new com.nimrodtechs.rsock.publisher.PublisherRSocket(this));
    }
    public Flux<Payload> addDirectProcessor(SubscriptionRequest subscriptionRequest) {
        SubscriberFluxInfo subscriberFluxInfo;
        if(subscriptionRequest.isWildcard()) {
            subscriberFluxInfo = wildcardProcessors.computeIfAbsent(subscriptionRequest.getSubject(), s -> {
                return new SubscriberFluxInfo(subscriptionRequest.getRequestor(), DirectProcessor.create(),subscriptionRequest.getSubject());
            });
        } else {
            subscriberFluxInfo = subjectProcessors.computeIfAbsent(subscriptionRequest.getSubject(), s -> {
                return new SubscriberFluxInfo(subscriptionRequest.getRequestor(), DirectProcessor.create(),subscriptionRequest.getSubject());
            });
        }
//        if(subscriptionRequest.isWildcard()) {
//            subscriberFluxInfo = wildcardProcessors.computeIfAbsent(subscriptionRequest.getSubject(), s -> {
//                return new SubscriberFluxInfo(subscriptionRequest.getRequestor(), Sinks.many().multicast().onBackpressureBuffer(),subscriptionRequest.getSubject());
//            });
//        } else {
//            subscriberFluxInfo = subjectProcessors.computeIfAbsent(subscriptionRequest.getSubject(), s -> {
//                return new SubscriberFluxInfo(subscriptionRequest.getRequestor(), Sinks.many().multicast().onBackpressureBuffer(),subscriptionRequest.getSubject());
//            });
//        }
        subscriberFluxInfo.directProcessor.doOnCancel(() -> {
            System.out.println(subscriptionRequest+" hase been canceled");
        });
        if(subscriberFluxInfo.subscriberNames.contains(subscriptionRequest.getRequestor()) == false) {
            subscriberFluxInfo.subscriberNames.add(subscriptionRequest.getRequestor());
        }
        //TODO delegate this to thread
        notifyListeners(subscriptionRequest);
        return subscriberFluxInfo.directProcessor;
        //return subscriberFluxInfo.sink.asFlux();
    }

    public void removeDirectProcessor(SubscriptionRequest subscriptionRequest) {
        SubscriberFluxInfo subscriberFluxInfo;
        if(subscriptionRequest.getSubject().endsWith("*")) {
            subscriberFluxInfo = wildcardProcessors.get(subscriptionRequest.getSubject());
        } else {
            subscriberFluxInfo = subjectProcessors.get(subscriptionRequest.getSubject());
        }
        if(subscriberFluxInfo != null) {
            if(subscriberFluxInfo.subscriberNames.contains(subscriptionRequest.getRequestor())) {
                subscriberFluxInfo.subscriberNames.remove(subscriptionRequest.getRequestor());
            }
            if(subscriberFluxInfo.subscriberNames.size() == 0) {
                subscriberFluxInfo.directProcessor.onComplete();
                if (subscriptionRequest.getSubject().endsWith("*")) {
                    wildcardProcessors.remove(subscriptionRequest.getSubject());
                } else {
                    subjectProcessors.remove(subscriptionRequest.getSubject());
                }
            }
            notifyListeners(subscriptionRequest);

        }

    }

    /**
     * If there are currently no subscribers then just store the latest value and return*
     * @param subject
     * @param data
     */
    public void publishData(String subject, Object data) {
        Payload payloadData = null;
        SubscriberFluxInfo subscriberFluxInfo = subjectProcessors.get(subject);
        HashSet<String> alreadySentSubscribers = new HashSet<>();
        if (subscriberFluxInfo != null) {
            // Serialize your data to Payload. Assuming you have a method serialize(Object data) to do that.
            PublisherPayload publisherPayload = new PublisherPayload(System.nanoTime(),subject,data);
            payloadData = DefaultPayload.create(KryoEncoder.serialize(publisherPayload));
            subscriberFluxInfo.directProcessor.onNext(payloadData);
            alreadySentSubscribers.add(subscriberFluxInfo.subscriberName);
        }
        if(wildcardProcessors.size() > 0) {
// 2 versions ... I want the efficiency of only doing serialization once !!!
//            wildcardProcessors.values().stream()
//                    .filter(entry -> subject.startsWith(entry.modifiedSubject) && alreadySentSubscribers.contains(entry.subscriberName)==false)
//                    .forEach(flux -> {
//                        if(payloadData == null) {
//                            PublisherPayload publisherPayload = new PublisherPayload(System.nanoTime(),subject,data);
//                            payloadData = DefaultPayload.create(KryoEncoder.serialize(publisherPayload));
//                        }
//                        flux.directProcessor.onNext(payloadData);});

            for (Iterator<Map.Entry<String, SubscriberFluxInfo>> it = wildcardProcessors.entrySet().iterator(); it.hasNext(); ) {
                SubscriberFluxInfo sfi  = it.next().getValue();
                if(subject.startsWith(sfi.modifiedSubject) && ! alreadySentSubscribers.contains(sfi.subscriberName)){
                    //See if I have already serialized the payload
                    if(payloadData == null) {
                        PublisherPayload publisherPayload = new PublisherPayload(System.nanoTime(),subject,data);
                        payloadData = DefaultPayload.create(KryoEncoder.serialize(publisherPayload));
                    }
                    sfi.directProcessor.onNext(payloadData);
                }
            }
        }
    }

    public void addSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.add(listener);
    }

    public void notifyListeners(SubscriptionRequest subscriptionRequest) {
        for(SubscriptionListener listener : subscriptionListeners){
            if(subscriptionRequest.getSubscriptionDirective() == SubscriptionDirective.REQUEST) {
                listener.onSubscription(subscriptionRequest);
            } else if(subscriptionRequest.getSubscriptionDirective() == SubscriptionDirective.CANCEL) {
                listener.onSubscriptionRemove(subscriptionRequest);
            }
        }

    }

}
