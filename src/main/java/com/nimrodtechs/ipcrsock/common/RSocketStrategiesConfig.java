package com.nimrodtechs.ipcrsock.common;

import com.nimrodtechs.ipcrsock.serialization.KryoCommon;
import com.nimrodtechs.ipcrsock.serialization.KryoDecoder;
import com.nimrodtechs.ipcrsock.serialization.KryoEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;

@Configuration
public class RSocketStrategiesConfig {
    @Bean
    public RSocketStrategies rSocketStrategies() {
        KryoCommon kryoCommon = new KryoCommon();
        return RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new KryoEncoder(kryoCommon)))
                .decoders(decoders -> decoders.add(new KryoDecoder(kryoCommon)))
                .build();
    }
}
