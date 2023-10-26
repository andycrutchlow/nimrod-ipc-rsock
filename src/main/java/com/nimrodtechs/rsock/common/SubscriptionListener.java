package com.nimrodtechs.rsock.common;

public interface SubscriptionListener {
    void onSubscription(SubscriptionRequest subscriptionRequest);
    void onSubscriptionRemove(SubscriptionRequest subscriptionRequest);
}
