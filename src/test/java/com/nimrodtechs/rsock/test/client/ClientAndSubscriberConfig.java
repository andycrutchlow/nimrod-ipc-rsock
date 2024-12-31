package com.nimrodtechs.rsock.test.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientAndSubscriberConfig {
    @Bean
    TestRemoteService testRemoteService() {
        return new TestRemoteService();
    }
}
