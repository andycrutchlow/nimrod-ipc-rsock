package com.nimrodtechs.ipcrsock.actuator;

import com.nimrodtechs.ipcrsock.publisher.PublisherSocketImpl;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "ipcrsock")
public class SocketMetrics {
    @ReadOperation
    public Object operation(@Selector String operationName) {
        switch (operationName) {
            case "publishLogOn":
                return publishLogOn();
            case "publishLogOff":
                return publishLogOff();
            default:
                return "Unknown operation";
        }
    }

    private String publishLogOn() {
        // logic for first operation
        PublisherSocketImpl.setLogLevel(1);
        return "Logging publish switched on";
    }

    private String publishLogOff() {
        // logic for second operation
        PublisherSocketImpl.setLogLevel(0);
        return "Logging publish switched off";
    }
}
