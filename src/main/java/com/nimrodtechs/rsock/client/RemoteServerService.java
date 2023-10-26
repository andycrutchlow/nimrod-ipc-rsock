package com.nimrodtechs.rsock.client;

import com.nimrodtechs.rsock.common.NimrodRmiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class RemoteServerService {
    @Autowired
    RSocketStrategies rSocketStrategies;

    @Autowired
    com.nimrodtechs.rsock.client.RemoteServerProperties remoteServerProperties;

    static class RemoteServerInfo {

        private String name;
        private String host;
        private int port;
        private int maxConcurrentCalls;
        //Override keepAliveWaitTime with a large number if you expect to use debugger in server side with breakpoints
        private int keepAliveWaitTime = 7200;
        private int keepAliveInterval = 2;
        private int retryReconnectInterval = 0;
        private long retryMaxAttempts =0;

        GenericObjectPool<RSocketRequester> connectionPool;

        public RemoteServerInfo(String name, String host, int port, int maxConcurrentCalls) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public String getName() {
            return name;
        }

        public GenericObjectPool<RSocketRequester> getConnectionPool() {
            return connectionPool;
        }

        void setConnectionPool(GenericObjectPool<RSocketRequester> connectionPool) {
            this.connectionPool = connectionPool;
        }
    }

    private Map<String, RemoteServerInfo> remoteServerInfoMap = new HashMap<>();


    @PostConstruct
    void init() throws Exception {
        for (String remoteServerInfoItems : remoteServerProperties.getSetup()) {
            String[] items = remoteServerInfoItems.split(",");
            RemoteServerInfo remoteServerInfo = new RemoteServerInfo(items[0], items[1], Integer.valueOf(items[2]), Integer.valueOf(items[3]));
            //Optional extra settings
            if (items.length > 4) {
                //Set this with a large number if you expect to use debugger in server side with breakpoints
                remoteServerInfo.keepAliveWaitTime = Integer.valueOf(items[4]);
            }
            if (items.length > 5) {
                remoteServerInfo.keepAliveInterval = Integer.valueOf(items[5]);
            }
            if (items.length > 6) {
                remoteServerInfo.retryMaxAttempts = Integer.valueOf(items[6]);
            }
            if (items.length > 7) {
                remoteServerInfo.retryReconnectInterval = Integer.valueOf(items[7]);
            }
            remoteServerInfo.setConnectionPool(getConnectPool(remoteServerInfo));
            remoteServerInfoMap.put(remoteServerInfo.getName(), remoteServerInfo);
        }
    }

    private GenericObjectPool<RSocketRequester> getConnectPool(RemoteServerInfo remoteServerInfo) throws Exception {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(remoteServerInfo.maxConcurrentCalls);
        poolConfig.setJmxEnabled(false);
        GenericObjectPool<RSocketRequester> pool = new GenericObjectPool<>(new PoolConnectionFactory(remoteServerInfo), poolConfig);
        //Initialize pool now with 4 pooled objects
        for (int i = 0; i < poolConfig.getMaxTotal(); i++) {
            try {
                RSocketRequester myObject = pool.borrowObject();
                pool.returnObject(myObject);
            } catch (Exception ex) {
                log.error("Error during initialization of Remote Server connection to " + remoteServerInfo.name + " host:" + remoteServerInfo.host + " port:" + remoteServerInfo.port);
                throw ex;
            }
        }
        log.info("Initialized " + remoteServerInfo.maxConcurrentCalls + " connections to server " + remoteServerInfo.getName() + ":" + remoteServerInfo.host + ":" + remoteServerInfo.port);
        return pool;
    }

    class PoolConnectionFactory extends BasePooledObjectFactory<RSocketRequester> {
        private final RemoteServerInfo remoteServerInfo;

        public PoolConnectionFactory(RemoteServerInfo remoteServerInfo) {
            this.remoteServerInfo = remoteServerInfo;
        }

        public RSocketRequester create() throws Exception {
            return getRSocketRequester(rSocketStrategies, remoteServerInfo);
        }

        // when an object is returned to the pool,
        @Override
        public void passivateObject(PooledObject<RSocketRequester> rSockReq) {
            //Do nothing
        }

        @Override
        public boolean validateObject(PooledObject<RSocketRequester> rSockReq) {
            if (rSockReq.getObject().isDisposed())
                return false;
            else
                return true;
        }

        @Override
        public void destroyObject(PooledObject<RSocketRequester> rSockReq) {
            log.info("Destroying rSockReq {}",rSockReq);
            rSockReq.getObject().dispose();
        }

        @Override
        public PooledObject<RSocketRequester> wrap(RSocketRequester rSockReq) {
            return new DefaultPooledObject<>(rSockReq);

        }

        // for all other methods, the no-op
        // implementation in BasePoolableObjectFactory
        // will suffice
    }

    /**
     * Kryo is added as a serializer *
     *
     * @return
     */
//    private RSocketStrategies rSocketStrategies() {
//        KryoCommon kryoCommon = new KryoCommon();
//        return RSocketStrategies.builder()
//                .encoders(encoders -> encoders.add(new KryoEncoder(kryoCommon)))
//                .decoders(decoders -> decoders.add(new KryoDecoder(kryoCommon)))
//                .build();
//    }

    private RSocketRequester getRSocketRequester(RSocketStrategies strategies, RemoteServerInfo remoteServerInfo) {
        RSocketRequester.Builder builder = RSocketRequester.builder();
        return builder
                .rsocketConnector(
                        rSocketConnector -> {
                            rSocketConnector.keepAlive(Duration.ofSeconds(remoteServerInfo.keepAliveInterval), Duration.ofSeconds(remoteServerInfo.keepAliveWaitTime));
                            rSocketConnector.reconnect(
                                    Retry.backoff(remoteServerInfo.retryMaxAttempts, Duration.ofSeconds(remoteServerInfo.retryReconnectInterval))
                            );
                        })
                .rsocketStrategies(strategies)
                .dataMimeType(new MimeType("application", "x-kryo"))
                .tcp(remoteServerInfo.host, remoteServerInfo.port);
    }


    public <T> T executeRmiMethod(Class<T> responseClass, String serviceName, String methodName, Object... parameters) throws Exception {
        RemoteServerInfo remoteServerInfo = remoteServerInfoMap.get(serviceName);
        if(remoteServerInfo == null) {
            throw new NimrodRmiException(serviceName+" is not a registered Remote Server");
        }
        RSocketRequester rsReq = null;
        AtomicBoolean resetConnectionPool = new AtomicBoolean(false);
        try {
            //This will block if all(maxConcurrentCalls) of the resource pool rsocketrequestors are currently occupied
            rsReq = remoteServerInfo.connectionPool.borrowObject();
            RSocketRequester.RequestSpec requestSpec = rsReq.route(methodName);
            //N.B. if method in the route corresponds to a fire-and-forget method in the server then it won't actually block
            T response = requestSpec.data(parameters).retrieveMono(responseClass)
                    .onErrorResume(e -> {
                        if (e.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException")) {
                            // Handle the closed channel condition, possibly by returning a fallback Mono or throwing a custom exception
                            resetConnectionPool.set(true);

                            return Mono.error(new Exception("Channel was closed!"));
                        }
                        // If the error is not the one we're looking for, propagate it unchanged
                        return Mono.error(e);
                    })
                    .block();

            return response;
        } catch (Exception ex) {
            throw ex;
        } finally {
            remoteServerInfo.connectionPool.returnObject(rsReq);
            if(resetConnectionPool.get()){
                remoteServerInfo.connectionPool.clear();
            }
        }
    }
}