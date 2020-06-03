package org.zhire.nettyproject;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.zhire.nettyproject.controller.web.WebsocketServer;

/**
 * Hello world!
 */
@SpringBootApplication
public class App implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        app.run(args);
        System.out.println("Hello World!");
    }

    @Override
    public void run(String... args) throws Exception {
        new WebsocketServer(7971).startNetty();
    }
}
