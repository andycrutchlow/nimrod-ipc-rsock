package com.nimrodtechs.ipcrsock.publisher;

import com.nimrodtechs.ipcrsock.serialization.KryoEncoder;
import com.nimrodtechs.ipcrsock.common.PublisherPayload;
import com.nimrodtechs.ipcrsock.common.SubscriptionDirective;
import com.nimrodtechs.ipcrsock.common.SubscriptionListener;
import com.nimrodtechs.ipcrsock.common.SubscriptionRequest;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class PublisherSocketImpl implements SocketAcceptor {

    @Value("${nimrod.rsock.publisher.port:-1}")
    int publisherPort;

    private static int logLevel = 0;

    public static void setLogLevel(int logLevel) {
        PublisherSocketImpl.logLevel = logLevel;
    }

    class SubscriberFluxInfo {
        String modifiedSubject;

        List<String> subscriberNames = new ArrayList<>();
        DirectProcessor<Payload> directProcessor;
        Sinks.Many<Payload> sink;

        public SubscriberFluxInfo(DirectProcessor<Payload> directProcessor, String unmodifiedSubject) {
            this.directProcessor = directProcessor;
            if(unmodifiedSubject.endsWith("*")) {
                modifiedSubject = unmodifiedSubject.substring(0,unmodifiedSubject.lastIndexOf("*"));
            } else {
                modifiedSubject = unmodifiedSubject;
            }
        }
        public SubscriberFluxInfo(Sinks.Many<Payload> sink, String unmodifiedSubject) {
            this.sink = sink;
            if(unmodifiedSubject.endsWith("*")) {
                modifiedSubject = unmodifiedSubject.substring(0,unmodifiedSubject.lastIndexOf("*"));
            } else {
                modifiedSubject = unmodifiedSubject;
            }
        }
        public List<String> getSubscriberNames() {
            return subscriberNames;
        }


    }

    //TODO add a high level TYPE which represents the data class that is serialized...then split wildcardProcessors into map based on type
    //to reduce the amount of iterating..
    private final Map<String, SubscriberFluxInfo> subjectProcessors = new ConcurrentHashMap<>();
    private final Map<String, SubscriberFluxInfo> wildcardProcessors = new ConcurrentHashMap<>();

    private final List<SubscriptionListener> subscriptionListeners = new ArrayList<>();

    @PostConstruct
    void init() {
        if (publisherPort != -1) {
            log.info("Configuring Nimrod RSocket Publisher on port "+publisherPort);
            RSocketServer.create(this)
                    .bind(TcpServerTransport.create(publisherPort)) // Specify your port here
                    .subscribeOn(Schedulers.boundedElastic()) // Use a different thread pool
                    .subscribe();

        }
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload, RSocket rSocket) {
        return Mono.just(new PublisherRSocket(this));
    }
    public Flux<Payload> addDirectProcessor(SubscriptionRequest subscriptionRequest) {
        SubscriberFluxInfo subscriberFluxInfo;
        if(subscriptionRequest.isWildcard()) {
            subscriberFluxInfo = wildcardProcessors.computeIfAbsent(subscriptionRequest.getSubject(), s -> {
                return new SubscriberFluxInfo(getDirectProcessor(subscriptionRequest),subscriptionRequest.getSubject());
            });
        } else {
            subscriberFluxInfo = subjectProcessors.computeIfAbsent(subscriptionRequest.getSubject(), s -> {
                return new SubscriberFluxInfo(getDirectProcessor(subscriptionRequest),subscriptionRequest.getSubject());
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
        //TODO delegate this to thread
        notifyListeners(subscriptionRequest);
        subscriberFluxInfo.getSubscriberNames().add(subscriptionRequest.getRequestor());
        return subscriberFluxInfo.directProcessor;
        //return subscriberFluxInfo.sink.asFlux();
    }

    private static <E> DirectProcessor<E> getDirectProcessor(SubscriptionRequest subscriptionRequest) {
        DirectProcessor directProcessor = DirectProcessor.create();
        directProcessor.doOnCancel(() -> {
            System.out.println(subscriptionRequest+" has been canceled");
        });
        directProcessor.doOnError(error -> {
            if (error instanceof CancellationException) {
                System.out.println(subscriptionRequest.toString()+" Subscription was cancelled");
            } else {
                System.err.println(subscriptionRequest.toString()+" An error occurred: " + error);
            }
        });
        directProcessor.doOnComplete(() -> {
            System.out.println(subscriptionRequest+" has been completed");
        });

        return directProcessor;

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
                log.info("REMOVED publishing of "+subscriptionRequest.getSubject()+" to "+subscriptionRequest.getRequestor());
            } else {
                log.info("REDUCED publishing of "+subscriptionRequest.getSubject()+" to "+subscriptionRequest.getRequestor()+", "+subscriberFluxInfo.subscriberNames.size()+" remain");
            }
            notifyListeners(subscriptionRequest);
        }
    }

    /**
     * If there are currently no subscribers then just store the latest value and return*
     * @param subject
     * @param data
     */
    public void publish(String subject, Object data) {
        if(publisherPort == -1) {
            log.warn("publish called subject="+subject+" class="+data.getClass().getSimpleName()+" but no nimrod.rsock.publisher.port called - IGNORED");
            return;
        }
        if(logLevel > 0) {
            log.info("PUBLISH:[{}]:[{}]",subject,data.toString());
        }
        Payload payloadData = null;
        SubscriberFluxInfo subscriberFluxInfo = subjectProcessors.get(subject);
        HashSet<String> alreadySentSubscribers = new HashSet<>();
        if (subscriberFluxInfo != null) {
            // Serialize your data to Payload. Assuming you have a method serialize(Object data) to do that.
            PublisherPayload publisherPayload = new PublisherPayload(System.nanoTime(),subject,data);
            payloadData = DefaultPayload.create(KryoEncoder.serialize(publisherPayload));
            subscriberFluxInfo.directProcessor.onNext(payloadData);
            alreadySentSubscribers.add(subscriberFluxInfo.getSubscriberNames().get(0));
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
                //Trying to protect against re-sending data when there is an exact subject match from above AND a wildcard subscription below..not sure that using first SubscriberNames will work ???
                if(subject.startsWith(sfi.modifiedSubject) && ! alreadySentSubscribers.contains(sfi.getSubscriberNames().get(0))){
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
