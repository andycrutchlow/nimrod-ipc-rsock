package com.nimrodtechs.rsock.test.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.awt.*;

@SpringBootApplication
//@ComponentScan(basePackages = {"com.nimrodtechs.rsock.publisher","com.nimrodtechs.rsock.test.server"})
@ComponentScan(basePackages = {"com.nimrodtechs.ipcrsock","com.nimrodtechs.rsock.test.server"})
public class ServerAndPublisherApplication {
    @Autowired
    ServerAndPublisherGui serverAndPublisherGui;

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .main(ServerAndPublisherApplication.class)
                .sources(ServerAndPublisherApplication.class)
                .profiles("serverAndPublisher")
                .run(args);

    }
    @PostConstruct
    void init() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                serverAndPublisherGui.pack();
                serverAndPublisherGui.setVisible(true);
            }
        });

    }
}
