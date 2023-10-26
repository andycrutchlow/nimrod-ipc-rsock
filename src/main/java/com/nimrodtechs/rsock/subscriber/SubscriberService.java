package com.nimrodtechs.rsock.subscriber;

import com.nimrodtechs.rsock.common.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import reactor.core.Disposable;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class SubscriberService {

    @Value("${spring.application.name}")
    String subscriberProcessName;

    @Autowired
    private com.nimrodtechs.rsock.subscriber.SubscriberProperties subscriberProperties;

    @Autowired
    RSocketStrategies rSocketStrategies;

    private Map<String, SubscriberConnectionInfo> subscriberInfoMap = new HashMap<>();

    Map<String, SubscriptionInfo> clientSubscriptions = new HashMap<>();

    static class SubscriptionInfo<T>  {
        String subject;
        Class<T> payloadClass;
        SubscriptionRequest originalSubscriptionRequest;

        Disposable disposable;

        public SubscriptionInfo(String subject, Class<T> payloadClass, SubscriptionRequest originalSubscriptionRequest) {
            this.subject = subject;
            this.payloadClass = payloadClass;
            this.originalSubscriptionRequest = originalSubscriptionRequest;
        }
        public Disposable getDisposable() {
            return disposable;
        }

        public void setDisposable(Disposable disposable) {
            this.disposable = disposable;
        }


    }

    static class SubscriberConnectionInfo {
        private String name;
        private String host;
        private int port;

        Map<String,List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry>> subjectListeners = new HashMap<>();
        Map<String,List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry>> wildcardListeners = new HashMap<>();

        com.nimrodtechs.rsock.subscriber.QueueExecutor conflatingQueueExecutor;
        com.nimrodtechs.rsock.subscriber.QueueExecutor sequentialQueueExecutor;

        private RSocketRequester rSocketRequester;

        public SubscriberConnectionInfo(String name, String host, int port) {
            this.name = name;
            this.host = host;
            this.port = port;
            conflatingQueueExecutor = new com.nimrodtechs.rsock.subscriber.ConflatingExecutor(name);
            sequentialQueueExecutor = new com.nimrodtechs.rsock.subscriber.SequentialExecutor(name);
            reSubscribeTimer = new Timer(name+"-reconnect-task",false);
        }

        String getName() {
            return name;
        }

        String getHost() {
            return host;
        }

        int getPort() {
            return port;
        }

        void setrSocketRequester(RSocketRequester rSocketRequester) {
            this.rSocketRequester = rSocketRequester;
        }

        RSocketRequester getRSocketRequester() {
            return rSocketRequester;
        }

        Timer reSubscribeTimer;

        TimerTask reSubscribeTimerTask;

    }

    class ReSubscribeTask extends TimerTask {
        private SubscriberConnectionInfo subscriberConnectionInfo;
        private Map<String, SubscriptionInfo> clientSubscriptions;

        public ReSubscribeTask(SubscriberConnectionInfo subscriberConnectionInfo,Map<String, SubscriptionInfo> clientSubscriptions) {
            this.subscriberConnectionInfo = subscriberConnectionInfo;
            this.clientSubscriptions = clientSubscriptions;
        }

        @Override
        public void run() {
            StringBuffer sb = new StringBuffer();
            subscriberConnectionInfo.subjectListeners.keySet().forEach(subscription -> {
                if(sb.length()>0) {
                    sb.append(",");
                }
                sb.append(subscription);
            });
            log.info("Try to resubscribe on publisher "+ subscriberConnectionInfo.getName()+" "+sb.toString());
            if(subscriberConnectionInfo.getRSocketRequester() != null) {
                subscriberConnectionInfo.getRSocketRequester().dispose();
            }
            subscriberConnectionInfo.setrSocketRequester(getRSocketRequester(subscriberConnectionInfo));
            for(String key : subscriberConnectionInfo.subjectListeners.keySet()) {
                String fullKey = subscriberConnectionInfo.getName()+":"+key;
                SubscriptionInfo subscriptionInfo = clientSubscriptions.get(fullKey);
                if(subscriptionInfo != null) {
                    if(subscriptionInfo.getDisposable().isDisposed() == false) {
                        subscriptionInfo.getDisposable().dispose();
                    }
                    Disposable disposable = establishFlux(subscriberConnectionInfo,subscriptionInfo);
                    subscriptionInfo.setDisposable(disposable);
                }
            }
            subscriberConnectionInfo.reSubscribeTimerTask.cancel();
            subscriberConnectionInfo.reSubscribeTimerTask = null;

        }
    }


    @PostConstruct
    void init() {
        for (String subscriberInfoItems : subscriberProperties.getSetup()) {
            String[] items = subscriberInfoItems.split(",");
            SubscriberConnectionInfo subscriberConnectionInfo = new SubscriberConnectionInfo(items[0], items[1], Integer.valueOf(items[2]));
            subscriberConnectionInfo.setrSocketRequester(getRSocketRequester(subscriberConnectionInfo));
            subscriberInfoMap.put(subscriberConnectionInfo.getName(), subscriberConnectionInfo);
        }
    }

    private RSocketRequester getRSocketRequester(SubscriberConnectionInfo subscriberConnectionInfo) {
        RSocketRequester.Builder builder = RSocketRequester.builder();
        //TODO make keepAlive settings parameters ... need long max duration when debugging
        return builder
                .rsocketConnector(
                        rSocketConnector -> {
                            rSocketConnector.keepAlive(Duration.ofSeconds(90),Duration.ofSeconds(7200));
                            rSocketConnector.reconnect(
                                    Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(2))
                            );
                        })
                .rsocketStrategies(rSocketStrategies)
                .dataMimeType(new MimeType("application", "x-kryo"))
                .tcp(subscriberConnectionInfo.getHost(), subscriberConnectionInfo.getPort());
    }

    public <T> void subscribe(String publisherName, String aSubject, MessageReceiverInterface<T> listener, Class<T> payloadClass, boolean conflate) throws NimrodPubSubException {
        SubscriberConnectionInfo subscriberConnectionInfo = subscriberInfoMap.get(publisherName);
        if(subscriberConnectionInfo == null) {
            throw new NimrodPubSubException(publisherName+" is not a valid publisher to send subscribe "+aSubject+" to");
        }
        List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry> messageProcessorEntries;
        boolean wildcard = aSubject.endsWith(".*");
        if(wildcard) {
            messageProcessorEntries = subscriberConnectionInfo.wildcardListeners.get(aSubject);
        } else {
            messageProcessorEntries = subscriberConnectionInfo.subjectListeners.get(aSubject);
        }



        //TODO Check if this listener already exists
        if(messageProcessorEntries == null) {
            messageProcessorEntries = new ArrayList<>();
            if(wildcard) {
                subscriberConnectionInfo.wildcardListeners.put(aSubject, messageProcessorEntries);
            } else {
                subscriberConnectionInfo.subjectListeners.put(aSubject, messageProcessorEntries);
            }
        }
        if(messageProcessorEntries.stream().anyMatch(entry -> entry.messageReceiver == listener)) {
            log.info(publisherName+" subject["+aSubject+"] listener "+listener.toString()+" already present : IGNORE");
            return;
        }

        messageProcessorEntries.add(new com.nimrodtechs.rsock.subscriber.MessageProcessorEntry(listener,conflate ? subscriberConnectionInfo.conflatingQueueExecutor: subscriberConnectionInfo.sequentialQueueExecutor));

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionDirective.REQUEST,subscriberProcessName,aSubject,wildcard);
        SubscriptionInfo<T> subscriptionInfo = new SubscriptionInfo(aSubject,payloadClass,subscriptionRequest);
        subscriptionInfo.setDisposable(establishFlux(subscriberConnectionInfo,subscriptionInfo));
        clientSubscriptions.put(publisherName+":"+aSubject, subscriptionInfo);
        log.info("SUBSCRIBE TO:"+publisherName+" "+" subject["+subscriptionRequest.getSubject()+"]");
    }

    private Disposable establishFlux(SubscriberConnectionInfo subscriberConnectionInfo, SubscriptionInfo subscriptionInfo) {
        RSocketRequester.RequestSpec requestSpec = subscriberConnectionInfo.getRSocketRequester().route(subscriberConnectionInfo.getName());
        Disposable disposable = requestSpec.data(subscriptionInfo.originalSubscriptionRequest).retrieveFlux(subscriptionInfo.payloadClass)
//                .doOnSubscribe(subscription -> {
//                    clientSubscriptions.put(publisherName+":"+aSubject, subscription);
//                })
                .subscribe(
                        messagePayload ->{dispatchMessage(subscriberConnectionInfo,subscriptionInfo.subject,messagePayload);},
                        error -> {handleFluxError(subscriberConnectionInfo.getName(),subscriptionInfo.originalSubscriptionRequest,error);},
                        ()-> { displayCompletion(subscriberConnectionInfo.getName(), subscriptionInfo.originalSubscriptionRequest);}
                );
        return disposable;
    }

    private void displayCompletion(String publisherName, SubscriptionRequest subscriptionRequest) {
        log.info(publisherName+" "+" subject["+subscriptionRequest.getSubject()+"] has closed gracefully");
    }

    private void handleFluxError(String publisherName, SubscriptionRequest subscriptionRequest,Object error) {
        log.info(publisherName+" "+" subject["+subscriptionRequest.getSubject()+"] ERROR : "+error);
        if(error instanceof ClosedChannelException|| error.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException")) {
            SubscriberConnectionInfo subscriberConnectionInfo = subscriberInfoMap.get(publisherName);
            if(subscriberConnectionInfo == null) {
                return;
            }
            //Start a timer task that loops trying to re-establish a good connection to the publisher and re-subscribe to any existing subscriptions found
            if(subscriberConnectionInfo.subjectListeners.size() > 0 || subscriberConnectionInfo.wildcardListeners.size() > 0) {
                if(subscriberConnectionInfo.reSubscribeTimerTask == null) {
                    subscriberConnectionInfo.reSubscribeTimerTask = new ReSubscribeTask(subscriberConnectionInfo,clientSubscriptions);
                    subscriberConnectionInfo.reSubscribeTimer.schedule(subscriberConnectionInfo.reSubscribeTimerTask,2000);
                }
            }
        }
    }

    /**
     * Remove the subscription for the listener.
     * If there are now no more listeners in this process for this subject then use fire-and-forget send to tell the publisher to stop publishing.
     * There show be a confirmation message back from the publisher telling us that DirectProcessor has be closed gracefully *
     * @param publisherName
     * @param aSubject
     * @param listener
     * @param <T>
     */
    public <T> void unsubscribe(String publisherName, String aSubject, MessageReceiverInterface<T> listener)  {
        SubscriberConnectionInfo subscriberConnectionInfo = subscriberInfoMap.get(publisherName);
        if(subscriberConnectionInfo == null) {
            log.error(publisherName+" is not a valid publisher to send unsubscribe "+aSubject+" to");
        }
        List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry> messageProcessorEntries;
        if(aSubject.endsWith("*")) {
            messageProcessorEntries = subscriberConnectionInfo.wildcardListeners.get(aSubject);
        } else {
            messageProcessorEntries = subscriberConnectionInfo.subjectListeners.get(aSubject);
        }
        //TODO Check if this listener already exists
        if(messageProcessorEntries == null) {
            log.info(publisherName+" subject["+aSubject+"] does not exist as a current subscription : IGNORE");
            return;
        }
        //Look for the listener in the current list of listeners and remove it
        com.nimrodtechs.rsock.subscriber.MessageProcessorEntry messageProcessorEntry = messageProcessorEntries.stream()
                .filter(entry -> entry.messageReceiver == listener)
                .findFirst()
                .orElse(null);

        if(messageProcessorEntry != null) {
            //messageDispatcher.queueExecutor.dispose();
            messageProcessorEntries.remove(messageProcessorEntry);
            log.info("REMOVED LISTENER:"+listener.getClass().getSimpleName()+" from "+publisherName+" "+" subject["+aSubject+"] remaining count="+ messageProcessorEntries.size());
        } else {
            log.info(publisherName+" subject["+aSubject+"] does not exist as a current subscription : IGNORE");
            return;
        }
        if(messageProcessorEntries.size() == 0) {
            Disposable disposable = clientSubscriptions.get(publisherName + ":" + aSubject).disposable;
            if(disposable != null) {
                disposable.dispose();
            }
            //Tell the publisher to stop publishing messages to this process for this subject
            RSocketRequester.RequestSpec requestSpec = subscriberConnectionInfo.getRSocketRequester().route(publisherName);
            requestSpec.data(new SubscriptionRequest(SubscriptionDirective.CANCEL,subscriberProcessName,aSubject)).send().subscribe();
            //TODO TEst this !!!!
            if(aSubject.endsWith("*")) {
                subscriberConnectionInfo.wildcardListeners.remove(aSubject);
            } else {
                subscriberConnectionInfo.subjectListeners.remove(aSubject);
            }
            log.info("UNSUBSCRIBE FROM:"+publisherName+" "+" subject["+aSubject+"]");
        }
    }

    private <T> void dispatchMessage(SubscriberConnectionInfo subscriberConnectionInfo, String originalSubject, T messagePayload) {
        //Look for exact match on originalSubject
        PublisherPayload publisherPayload = (PublisherPayload)messagePayload;
        HashSet<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry> listenersToBeNotified = new HashSet<>();
        List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry> messageProcessorEntries = subscriberConnectionInfo.subjectListeners.get(publisherPayload.getSubject());
        if(messageProcessorEntries != null) {
            for (com.nimrodtechs.rsock.subscriber.MessageProcessorEntry messageProcessorEntry : messageProcessorEntries) {
                listenersToBeNotified.add(messageProcessorEntry);
            }
        }
        //Look for any wildcard matches...hashset will ensure that a listener will only be called once
        if(subscriberConnectionInfo.wildcardListeners.size() > 0) {
            for (Iterator<Map.Entry<String, List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry>>> it = subscriberConnectionInfo.wildcardListeners.entrySet().iterator(); it.hasNext(); ) {
                //PublisherSocketImpl.SubscriberFluxInfo sfi = it.next().getValue();
                Map.Entry<String, List<com.nimrodtechs.rsock.subscriber.MessageProcessorEntry>> next = it.next();
                String modifiedSubject = originalSubject.contains("*") ? next.getKey().substring(0,originalSubject.lastIndexOf("*")) : originalSubject;
                if(publisherPayload.getSubject().length() > modifiedSubject.length() && publisherPayload.getSubject().startsWith(modifiedSubject)) {
                    for(com.nimrodtechs.rsock.subscriber.MessageProcessorEntry messageProcessorEntry : next.getValue()) {
                        listenersToBeNotified.add(messageProcessorEntry);
                    }
                }
            }
        }
        //At this point hand off the calls to the various listeners for this message to an executor queue.
        listenersToBeNotified.stream().forEach( entry -> entry.queueExecutor.process(publisherPayload,entry));

        //listenersToBeNotified.stream().forEach( entry -> entry.messageReceiver.messageReceived(subscriberInfo.getName(),publisherPayload.getSubject(),publisherPayload.getPayload()));
    }
}