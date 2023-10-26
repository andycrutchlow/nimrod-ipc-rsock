package com.nimrodtechs.rsock.common;

import com.nimrodtechs.rsock.serialization.KryoCommon;
import com.nimrodtechs.rsock.serialization.KryoDecoder;
import com.nimrodtechs.rsock.serialization.KryoEncoder;
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
