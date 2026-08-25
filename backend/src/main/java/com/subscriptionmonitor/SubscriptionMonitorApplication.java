package com.subscriptionmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubscriptionMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionMonitorApplication.class, args);
    }
}
