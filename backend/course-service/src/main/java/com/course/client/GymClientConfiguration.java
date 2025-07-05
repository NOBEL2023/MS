package com.course.client;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration commune pour les clients Feign
 */
@Configuration
public class GymClientConfiguration {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5000,  // connectTimeout en millisecondes
            10000  // readTimeout en millisecondes
        );
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
            1000,  // période initiale en ms
            3000,  // période maximale en ms
            3      // nombre maximum de tentatives
        );
    }
}