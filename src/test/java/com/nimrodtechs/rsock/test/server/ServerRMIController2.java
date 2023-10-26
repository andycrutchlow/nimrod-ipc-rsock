package com.nimrodtechs.rsock.test.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Slf4j
@Controller
public class ServerRMIController2 {

    @PostConstruct
    void init() {
        System.out.println("HERE ServerRMIController2");
    }

    int i = 100;
    @MessageMapping("getMarketData2")
    public Mono<String> getMarketData2(Object[] params) {
        log.info("getMarketData2 "+params);
        return Mono.just("getMarketData2 response "+i++);
    }

    @MessageMapping("postMarketData2")
    public Mono<Void> postMarketData2(Object[] params) {
        log.info("postMarketData2 "+params);
        return Mono.empty();
    }
}
