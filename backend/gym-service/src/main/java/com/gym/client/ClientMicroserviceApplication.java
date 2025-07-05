package com.gym.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.gym.client.feign")  // OU le package exact où est GymServiceClient
public class ClientMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientMicroserviceApplication.class, args);
    }
}
